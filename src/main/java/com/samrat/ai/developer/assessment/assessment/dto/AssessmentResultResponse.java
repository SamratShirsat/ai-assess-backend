package com.samrat.ai.developer.assessment.assessment.dto;

import com.samrat.ai.developer.assessment.assessment.model.AssessmentStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record AssessmentResultResponse(
        Long sessionId,
        Long assessmentId,
        String assessmentTitle,
        AssessmentStatus status,
        BigDecimal totalScore,
        Integer totalPoints,
        BigDecimal percentage,
        Boolean passed,
        Instant startedAt,
        Instant submittedAt,
        Long durationSeconds,
        Integer problemsAttempted,
        Integer problemsSolved
) {}