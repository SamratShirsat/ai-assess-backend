package com.samrat.ai.developer.assessment.submission.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import com.samrat.ai.developer.assessment.execution.model.SupportedLanguage;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.samrat.ai.developer.assessment.submission.model.SubmissionStatus;

public class SubmissionDTOs {

    @Data
    public static class CodeSubmissionRequest {
        @NotNull @Min(1)
        private Long problemId;

        @NotNull
        private SupportedLanguage language;

        @NotBlank
        private String sourceCode;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestCaseRunResult {
        private Long testCaseId;
        private boolean passed;
        private boolean hidden;
        private boolean sample;
        private String input;          // Non-null for sample tests only
        private String expectedOutput; // Non-null for sample tests only
        private String actualOutput;   // Non-null for sample tests only
        private String errorMessage;
        private long executionTimeMs;
    }
    public record SubmissionCreateRequest(
            @NotNull(message = "Problem ID is required")
            Long problemId,

            @NotBlank(message = "Language is required")
            String language,

            @NotBlank(message = "Source code cannot be empty")
            @Size(max = 100000, message = "Source code exceeds maximum allowed size (100KB)")
            String sourceCode
    ) {}

    public record SubmissionResponse(
            Long id,
            Long userId,
            String userName,
            Long problemId,
            String problemTitle,
            String language,
            SubmissionStatus status,
            BigDecimal score,
            int totalPoints,
            int passedTests,
            int totalTests,
            Long executionTimeMs,
            String errorMessage,
            Instant submittedAt
    ) {}

    public record SubmissionSummaryResponse(
            Long id,
            Long problemId,
            String problemTitle,
            String language,
            SubmissionStatus status,
            BigDecimal score,
            int passedTests,
            int totalTests,
            Long executionTimeMs,
            Instant submittedAt
    ) {}

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CodeSubmissionResponse {
        private Long submissionId;
        private Long problemId;
        private String problemTitle;
        private SupportedLanguage language;
        private SubmissionStatus status;
        private Integer score;
        private Integer totalPoints;
        private Integer passedTests;
        private Integer totalTests;
        private Long executionTimeMs;
        private LocalDateTime submittedAt;
        private List<TestCaseRunResult> testResults;
    }
}
