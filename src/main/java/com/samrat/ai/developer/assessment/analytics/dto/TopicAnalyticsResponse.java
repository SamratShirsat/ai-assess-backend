package com.samrat.ai.developer.assessment.analytics.dto;

import com.samrat.ai.developer.assessment.adaptive.model.SkillLevel;
import com.samrat.ai.developer.assessment.analytics.model.PerformanceTrend;
import com.samrat.ai.developer.assessment.problem.model.Topic;

import java.math.BigDecimal;

public record TopicAnalyticsResponse(
        Topic topic,
        double skillScore,
        SkillLevel skillLevel,
        long attempted,
        long solved,
        BigDecimal successRate,
        BigDecimal averageScore,
        PerformanceTrend trend
) {}
