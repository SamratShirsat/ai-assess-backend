package com.samrat.ai.developer.assessment.adaptive.dto;

import com.samrat.ai.developer.assessment.adaptive.model.DiagnosticAssessmentStatus;
import com.samrat.ai.developer.assessment.adaptive.model.RecommendationReasonType;
import com.samrat.ai.developer.assessment.adaptive.model.SkillLevel;
import com.samrat.ai.developer.assessment.problem.model.Difficulty;
import com.samrat.ai.developer.assessment.problem.model.Topic;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class AdaptiveDTOs {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillPerformanceResponse {
        private Topic topic;
        private Double score;
        private SkillLevel level;
        private Double successRate;
        private Integer attempts;
        private Integer acceptedAttempts;
        private Difficulty currentDifficulty;
        private Double easySuccessRate;
        private Double mediumSuccessRate;
        private Double hardSuccessRate;
        private Double consistencyScore;
        private LocalDateTime lastAttemptAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserSkillProfileResponse {
        private Double overallScore;
        private SkillLevel overallLevel;
        private LocalDateTime lastCalculatedAt;
        private List<SkillPerformanceResponse> skills;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MinimalProblemResponse {
        private Long id;
        private String title;
        private Difficulty difficulty;
        private Topic topic;
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdaptiveRecommendationResponse {
        private Long recommendationId;
        private MinimalProblemResponse problem;
        private Topic topic;
        private Difficulty difficulty;
        private RecommendationReasonType reasonType;
        private String reason;
        private Double skillScore;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiagnosticProblemResponse {
        private Integer sequenceNumber;
        private MinimalProblemResponse problem;
        private boolean completed;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiagnosticAssessmentResponse {
        private Long id;
        private DiagnosticAssessmentStatus status;
        private Integer currentQuestionIndex;
        private Integer totalQuestions;
        private List<DiagnosticProblemResponse> questions;
    }
}
