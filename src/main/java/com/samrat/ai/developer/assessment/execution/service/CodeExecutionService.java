package com.samrat.ai.developer.assessment.execution.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.samrat.ai.developer.assessment.exception.ProblemNotFoundException;
import com.samrat.ai.developer.assessment.execution.config.CodeExecutionProperties;
import com.samrat.ai.developer.assessment.execution.dto.CodeExecutionRequest;
import com.samrat.ai.developer.assessment.execution.dto.CodeExecutionResponse;
import com.samrat.ai.developer.assessment.problem.repository.ProblemRepository;
import com.samrat.ai.developer.assessment.submission.exception.SubmissionExceptions;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CodeExecutionService {

    private final ProblemRepository problemRepository;
    private final DockerExecutionService dockerExecutionService;
    private final CodeExecutionProperties properties;

    private final Map<String, UserRateLimit> rateLimitMap = new ConcurrentHashMap<>();

    public CodeExecutionResponse executeCode(CodeExecutionRequest request, String userEmail) {
        checkRateLimit(userEmail);

        if (!problemRepository.existsById(request.getProblemId())) {
            throw new ProblemNotFoundException("Problem not found with id: " + request.getProblemId());
        }

        log.info("Code execution requested: user={}, problemId={}, language={}", userEmail, request.getProblemId(), request.getLanguage());

        CodeExecutionResponse response = dockerExecutionService.execute(
                request.getLanguage(), request.getSourceCode());

        log.info("Code execution completed: user={}, problemId={}, status={}, executionTimeMs={}",
                userEmail, request.getProblemId(), response.getStatus(), response.getMaxExecutionTimeMs());

        return response;
    }

    private void checkRateLimit(String userEmail) {
        long now = System.currentTimeMillis();
        UserRateLimit limit = rateLimitMap.computeIfAbsent(userEmail, k -> new UserRateLimit(now));

        synchronized (limit) {
            if (now - limit.windowStart > 60000) {
                limit.windowStart = now;
                limit.count.set(0);
            }
            if (limit.count.incrementAndGet() > properties.getMaxExecutionsPerMinute()) {
                throw new SubmissionExceptions.RateLimitExceededException("Execution limit reached (Maximum " + properties.getMaxExecutionsPerMinute() + " executions per minute).");
            }
        }
    }

    private static class UserRateLimit {
        long windowStart;
        AtomicInteger count = new AtomicInteger(0);

        UserRateLimit(long windowStart) {
            this.windowStart = windowStart;
        }
    }
}
