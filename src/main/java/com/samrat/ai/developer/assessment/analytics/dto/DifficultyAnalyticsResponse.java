package com.samrat.ai.developer.assessment.analytics.dto;

import com.samrat.ai.developer.assessment.problem.model.Difficulty;

import java.math.BigDecimal;

public record DifficultyAnalyticsResponse(
        Difficulty difficulty,
        long attempts,
        long solved,
        BigDecimal successRate,
        BigDecimal averageScore
) {}
