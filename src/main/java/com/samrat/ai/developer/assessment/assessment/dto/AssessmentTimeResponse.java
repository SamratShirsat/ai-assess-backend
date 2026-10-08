package com.samrat.ai.developer.assessment.assessment.dto;

import com.samrat.ai.developer.assessment.assessment.model.AssessmentStatus;
import java.time.Instant;

public record AssessmentTimeResponse(
        Instant serverTime,
        Instant expiresAt,
        AssessmentStatus status,
        Long remainingSeconds
) {}