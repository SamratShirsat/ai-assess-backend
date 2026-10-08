package com.samrat.ai.developer.assessment.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.samrat.ai.developer.assessment.ai.client.AiClient;
import com.samrat.ai.developer.assessment.ai.config.AiProperties;
import com.samrat.ai.developer.assessment.ai.dto.CodeReviewRequest;
import com.samrat.ai.developer.assessment.ai.dto.CodeReviewResponse;
import com.samrat.ai.developer.assessment.ai.model.AiReview;
import com.samrat.ai.developer.assessment.ai.exception.AiRateLimitException;
import com.samrat.ai.developer.assessment.ai.repository.AiReviewRepository;
import com.samrat.ai.developer.assessment.problem.model.Problem;
import com.samrat.ai.developer.assessment.submission.model.Submission;
import com.samrat.ai.developer.assessment.problem.repository.ProblemRepository;
import com.samrat.ai.developer.assessment.submission.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiCodeReviewService {

    private final AiClient aiClient;
    private final AiPromptService promptService;
    private final AiResponseParser responseParser;
    private final AiReviewRepository aiReviewRepository;
    private final SubmissionRepository submissionRepository;
    private final ProblemRepository problemRepository;
    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper;

    @Transactional
    public CodeReviewResponse reviewCode(Long userId, CodeReviewRequest request) {
        checkRateLimit(userId);

        Submission submission = null;
        Problem problem;
        String sourceCode;
        String language;

        if (request.getSubmissionId() != null) {
            submission = submissionRepository.findById(request.getSubmissionId())
                    .orElseThrow(() -> new IllegalArgumentException("Submission not found"));

            if (!submission.getId().equals(userId)) {
                throw new SecurityException("Unauthorized access to submission");
            }

            // Cache hit check
            Optional<AiReview> existingReview = aiReviewRepository.findByUserIdAndSubmissionId(userId, submission.getId());
            if (existingReview.isPresent()) {
                return mapToResponse(existingReview.get());
            }

            problem = submission.getProblem();
            sourceCode = submission.getSourceCode();
            language = submission.getLanguage().toString();
        } else {
            if (request.getProblemId() == null) {
                throw new IllegalArgumentException("Either submissionId or problemId must be provided");
            }
            problem = problemRepository.findById(request.getProblemId())
                    .orElseThrow(() -> new IllegalArgumentException("Problem not found"));
            sourceCode = request.getSourceCode();
            language = request.getLanguage() != null ? request.getLanguage() : "JAVA";
        }

        if (sourceCode == null || sourceCode.isBlank()) {
            throw new IllegalArgumentException("Source code cannot be empty for review.");
        }

        // Construct Prompts
        String systemPrompt = promptService.buildReviewSystemPrompt();
        String userPrompt = promptService.buildReviewUserPrompt(
                problem.getTitle(),
                problem.getDescription(),
                problem.getConstraints(),
                language,
                sourceCode,
                submission != null ? submission.getStatus().name() : null,
                submission != null ? submission.getScore() : null,
                submission != null ? submission.getPassedTests() : null,
                submission != null ? submission.getTotalTests() : null
        );

        String rawResponse = aiClient.generate(systemPrompt, userPrompt);
        CodeReviewResponse parsed = responseParser.parseCodeReview(rawResponse);

        parsed.setSubmissionId(submission != null ? submission.getId() : null);
        parsed.setProblemId(problem.getId());

        // Persist AI Review
        try {
            AiReview aiReview = AiReview.builder()
                    .userId(userId)
                    .submissionId(submission != null ? submission.getId() : null)
                    .problemId(problem.getId())
                    .language(language)
                    .summary(parsed.getSummary())
                    .reviewJson(objectMapper.writeValueAsString(parsed))
                    .build();

            AiReview saved = aiReviewRepository.save(aiReview);
            parsed.setId(saved.getId());
        } catch (Exception e) {
            log.error("Failed to persist AI Review record", e);
        }

        return parsed;
    }

    public List<CodeReviewResponse> getUserReviews(Long userId) {
        return aiReviewRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public CodeReviewResponse getReviewById(Long userId, Long reviewId) {
        AiReview review = aiReviewRepository.findByIdAndUserId(reviewId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found or access denied"));
        return mapToResponse(review);
    }

    private CodeReviewResponse mapToResponse(AiReview review) {
        try {
            CodeReviewResponse res = objectMapper.readValue(review.getReviewJson(), CodeReviewResponse.class);
            res.setId(review.getId());
            res.setSubmissionId(review.getSubmissionId());
            res.setProblemId(review.getProblemId());
            return res;
        } catch (Exception e) {
            log.error("Error deserializing review JSON", e);
            return CodeReviewResponse.builder()
                    .id(review.getId())
                    .summary(review.getSummary())
                    .build();
        }
    }

    private void checkRateLimit(Long userId) {
        OffsetDateTime since = OffsetDateTime.now().minusMinutes(aiProperties.getRateLimitWindowMinutes());
        long count = aiReviewRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .filter(r -> r.getCreatedAt().isAfter(since))
                .count();

        if (count >= aiProperties.getReviewRateLimit()) {
            throw new AiRateLimitException("AI Code Review rate limit exceeded. Please wait before trying again.");
        }
    }
}