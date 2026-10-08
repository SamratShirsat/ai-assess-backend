package com.samrat.ai.developer.assessment.analytics.dto;

import com.samrat.ai.developer.assessment.problem.model.Topic;

public record TopicScoreSummary(
        Topic topic,
        int score
) {}
