package com.samrat.ai.developer.assessment.analytics.dto;

import java.util.List;

public record DashboardResponse(
        OverallAnalyticsResponse overall,
        List<TopicScoreSummary> strongestTopics,
        List<TopicScoreSummary> weakestTopics,
        List<TopicAnalyticsResponse> topicAnalytics,
        List<DifficultyAnalyticsResponse> difficultyAnalytics,
        List<RecentActivityResponse> recentActivity
) {}
