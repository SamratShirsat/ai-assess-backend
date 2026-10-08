package com.samrat.ai.developer.assessment.assessment.dto;

import com.samrat.ai.developer.assessment.assessment.model.AssessmentStatus;
import java.time.Instant;

public record StartAssessmentResponse(
        Long sessionId,
        Long assessmentId,
        AssessmentStatus status,
        Instant startedAt,
        Instant expiresAt,
        Instant serverTime,
        Integer totalProblems,
        Long currentProblemId
) {}