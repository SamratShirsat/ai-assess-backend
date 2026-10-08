package com.samrat.ai.developer.assessment.submission.service;


import com.samrat.ai.developer.assessment.adaptive.service.AdaptiveAssessmentService;
import com.samrat.ai.developer.assessment.exception.ExecutionExceptions;
import com.samrat.ai.developer.assessment.exception.ProblemNotFoundException;
import com.samrat.ai.developer.assessment.execution.config.CodeExecutionProperties;
import com.samrat.ai.developer.assessment.execution.dto.CodeExecutionResponse;
import com.samrat.ai.developer.assessment.execution.dto.ExecutionStatus;
import com.samrat.ai.developer.assessment.execution.model.SupportedLanguage;
import com.samrat.ai.developer.assessment.execution.service.DockerExecutionService;
import com.samrat.ai.developer.assessment.problem.model.Problem;
import com.samrat.ai.developer.assessment.problem.repository.ProblemRepository;
import com.samrat.ai.developer.assessment.submission.dto.SubmissionDTOs;
import com.samrat.ai.developer.assessment.submission.model.Submission;
import com.samrat.ai.developer.assessment.submission.model.SubmissionStatus;
import com.samrat.ai.developer.assessment.testcase.model.TestCase;
import com.samrat.ai.developer.assessment.testcase.repository.TestCaseRepository;
import com.samrat.ai.developer.assessment.user.model.User;
import com.samrat.ai.developer.assessment.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.samrat.ai.developer.assessment.submission.repository.SubmissionRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionEvaluationService {

    private final ProblemRepository problemRepository;
    private final TestCaseRepository testCaseRepository;
    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final DockerExecutionService dockerExecutionService;
    private final OutputComparisonService outputComparisonService;
    private final CodeExecutionProperties executionProperties;
    private final AdaptiveAssessmentService adaptiveAssessmentService;

    private final Map<String, UserSubmissionLimit> rateLimitMap = new ConcurrentHashMap<>();
    private final Semaphore evaluationConcurrencyLimiter = new Semaphore(4);

    public SubmissionDTOs.CodeSubmissionResponse evaluate(SubmissionDTOs.CodeSubmissionRequest request, String userEmail) {
        enforceSubmissionRateLimit(userEmail);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userEmail));

        Problem problem = problemRepository.findById(request.getProblemId())
                .orElseThrow(() -> new ProblemNotFoundException("Problem not found with id: " + request.getProblemId()));

        List<TestCase> testCases = testCaseRepository.findByProblemIdOrderByIdAsc(problem.getId());
        if (testCases.isEmpty()) {
            throw new IllegalStateException("Problem " + problem.getId() + " has no configured test cases.");
        }

        try {
            evaluationConcurrencyLimiter.acquire();
            return doEvaluation(user, problem, request.getLanguage(), request.getSourceCode(), testCases);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Evaluation interrupted while waiting for system worker slot.");
        } finally {
            evaluationConcurrencyLimiter.release();
        }
    }

    private SubmissionDTOs.CodeSubmissionResponse doEvaluation(
            User user,
            Problem problem,
            SupportedLanguage language,
            String sourceCode,
            List<TestCase> testCases
    ) {
        List<SubmissionDTOs.TestCaseRunResult> results = new ArrayList<>();
        int totalPoints = testCases.stream().mapToInt(TestCase::getPoints).sum();
        int earnedPoints = 0;
        int passedCount = 0;
        long maxExecutionTime = 0;

        SubmissionStatus overallStatus = SubmissionStatus.ACCEPTED;
        boolean earlyTermination = false;

        for (TestCase tc : testCases) {
            if (earlyTermination) {
                results.add(buildUnexecutedResult(tc));
                continue;
            }

            CodeExecutionResponse response = dockerExecutionService.executeWithInput(
                    language,
                    sourceCode,
                    tc.getInput()
            );

            long execTime = response.getExecutionTimeMs();
            if (execTime > maxExecutionTime) {
                maxExecutionTime = execTime;
            }

            if (response.getStatus() == ExecutionStatus.COMPILE_ERROR) {
                overallStatus = SubmissionStatus.COMPILE_ERROR;
                earlyTermination = true;
                results.add(buildResult(tc, false, response.getStderr(), "Compilation Failed", execTime));
                continue;
            }

            if (response.getStatus() == ExecutionStatus.TIME_LIMIT_EXCEEDED) {
                if (overallStatus == SubmissionStatus.ACCEPTED) overallStatus = SubmissionStatus.TIME_LIMIT_EXCEEDED;
                results.add(buildResult(tc, false, null, "Time Limit Exceeded", execTime));
                continue;
            }

            if (response.getStatus() == ExecutionStatus.RUNTIME_ERROR) {
                if (overallStatus == SubmissionStatus.ACCEPTED) overallStatus = SubmissionStatus.RUNTIME_ERROR;
                results.add(buildResult(tc, false, response.getStderr(), "Runtime Error", execTime));
                continue;
            }

            if (response.getStatus() != ExecutionStatus.SUCCESS) {
                if (overallStatus == SubmissionStatus.ACCEPTED) overallStatus = SubmissionStatus.SYSTEM_ERROR;
                results.add(buildResult(tc, false, null, response.getMessage(), execTime));
                continue;
            }

            boolean isMatch = outputComparisonService.compare(tc.getExpectedOutput(), response.getStdout());
            if (isMatch) {
                earnedPoints += tc.getPoints();
                passedCount++;
                results.add(buildResult(tc, true, response.getStdout(), null, execTime));
            } else {
                if (overallStatus == SubmissionStatus.ACCEPTED) overallStatus = SubmissionStatus.WRONG_ANSWER;
                results.add(buildResult(tc, false, response.getStdout(), "Wrong Answer", execTime));
            }
        }

        if (passedCount > 0 && passedCount < testCases.size() && overallStatus == SubmissionStatus.WRONG_ANSWER) {
            overallStatus = SubmissionStatus.PARTIALLY_ACCEPTED;
        }

        int scorePercentage = (int) Math.round(((double) earnedPoints / totalPoints) * 100);

        Submission submission = Submission.builder()
                .user(user)
                .problem(problem)
                .language(language)
                .sourceCode(sourceCode)
                .status(overallStatus)
                .score(scorePercentage)
                .totalPoints(totalPoints)
                .passedTests(passedCount)
                .totalTests(testCases.size())
                .executionTimeMs(maxExecutionTime)
                .build();

        submission = submissionRepository.save(submission);
        try {
            adaptiveAssessmentService.updateAfterSubmission(user.getId(), submission.getId());
        } catch (Exception e) {
            log.error("Failed to update adaptive profile following submission {}", submission.getId(), e);
        }
        log.info("Submission saved: id={}, user={}, problem={}, status={}, score={}%",
                submission.getId(), user.getEmail(), problem.getId(), overallStatus, scorePercentage);

        return SubmissionDTOs.CodeSubmissionResponse.builder()
                .submissionId(submission.getId())
                .problemId(problem.getId())
                .problemTitle(problem.getTitle())
                .language(language)
                .status(overallStatus)
                .score(scorePercentage)
                .totalPoints(totalPoints)
                .passedTests(passedCount)
                .totalTests(testCases.size())
                .executionTimeMs(maxExecutionTime)
                .submittedAt(submission.getSubmittedAt())
                .testResults(results)
                .build();
    }

    private SubmissionDTOs.TestCaseRunResult buildResult(TestCase tc, boolean passed, String actualOutput, String errorMsg, long time) {
        return SubmissionDTOs.TestCaseRunResult.builder()
                .testCaseId(tc.getId())
                .passed(passed)
                .hidden(tc.isHidden())
                .sample(tc.isSample())
                .input(tc.isHidden() ? null : tc.getInput())
                .expectedOutput(tc.isHidden() ? null : tc.getExpectedOutput())
                .actualOutput(tc.isHidden() ? null : actualOutput)
                .errorMessage(errorMsg)
                .executionTimeMs(time)
                .build();
    }

    private SubmissionDTOs.TestCaseRunResult buildUnexecutedResult(TestCase tc) {
        return SubmissionDTOs.TestCaseRunResult.builder()
                .testCaseId(tc.getId())
                .passed(false)
                .hidden(tc.isHidden())
                .sample(tc.isSample())
                .errorMessage("Skipped due to prior execution failure")
                .executionTimeMs(0)
                .build();
    }

    private void enforceSubmissionRateLimit(String userEmail) {
        long now = System.currentTimeMillis();
        UserSubmissionLimit limit = rateLimitMap.computeIfAbsent(userEmail, k -> new UserSubmissionLimit(now));

        synchronized (limit) {
            if (now - limit.windowStart > 60000) {
                limit.windowStart = now;
                limit.count.set(0);
            }
            if (limit.count.incrementAndGet() > 5) {
                throw new ExecutionExceptions.RateLimitExceededException("Submission rate limit reached (Maximum 5 submissions per minute).");
            }
        }
    }

    private static class UserSubmissionLimit {
        long windowStart;
        AtomicInteger count = new AtomicInteger(0);

        UserSubmissionLimit(long windowStart) {
            this.windowStart = windowStart;
        }
    }
}
