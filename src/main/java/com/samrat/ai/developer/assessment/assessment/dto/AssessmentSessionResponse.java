package com.samrat.ai.developer.assessment.assessment.dto;

import com.samrat.ai.developer.assessment.assessment.model.AssessmentProblemStatus;
import com.samrat.ai.developer.assessment.assessment.model.AssessmentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record AssessmentSessionResponse(
        Long sessionId,
        Long assessmentId,
        String assessmentTitle,
        AssessmentStatus status,
        Instant startedAt,
        Instant expiresAt,
        Instant serverTime,
        Long currentProblemId,
        BigDecimal totalScore,
        BigDecimal percentage,
        List<SessionProblemOverview> problems
) {
    public record SessionProblemOverview(
            Long problemId,
            String title,
            Integer displayOrder,
            Integer points,
            AssessmentProblemStatus status,
            BigDecimal score,
            Integer attemptCount
    ) {}
}