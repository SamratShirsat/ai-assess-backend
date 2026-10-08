package com.samrat.ai.developer.assessment.analytics.dto;

import com.samrat.ai.developer.assessment.adaptive.model.SkillLevel;
import com.samrat.ai.developer.assessment.analytics.model.PerformanceTrend;
import com.samrat.ai.developer.assessment.problem.model.Topic;

import java.math.BigDecimal;

public record OverallAnalyticsResponse(
        double skillScore,
        SkillLevel skillLevel,
        long totalProblems,
        long solvedProblems,
        long totalSubmissions,
        long acceptedSubmissions,
        BigDecimal acceptanceRate,
        BigDecimal averageScore,
        Topic strongestTopic,
        Topic weakestTopic,
        PerformanceTrend overallTrend
) {}

