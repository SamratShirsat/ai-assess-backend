package com.samrat.ai.developer.assessment.analytics.controller;

import com.samrat.ai.developer.assessment.analytics.dto.*;
import com.samrat.ai.developer.assessment.analytics.model.PerformanceTrend;
import com.samrat.ai.developer.assessment.analytics.service.AnalyticsService;
import com.samrat.ai.developer.assessment.problem.model.Topic;
import com.samrat.ai.developer.assessment.user.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    // Helper method to safely extract the User ID from the Security Context
    private Long extractUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }

        Object principal = authentication.getPrincipal();

        // Scenario 1: Principal is your database User entity
        if (principal instanceof User) {
            return ((User) principal).getId();
        }

        // Scenario 2: Principal is a CustomUserDetails object
        // (Using reflection here so you don't have to fix imports, it dynamically calls getId())
        try {
            return (Long) principal.getClass().getMethod("getId").invoke(principal);
        } catch (Exception e) {
            // Scenario 3: Principal is just a String (username/email), which means your JWT
            // filter isn't saving the ID. We throw a clear error so you know what's wrong.
            throw new IllegalStateException("Could not extract User ID from Principal: " + principal.getClass().getName());
        }
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard(Authentication authentication) {
        Long userId = extractUserId(authentication);
        return ResponseEntity.ok(analyticsService.getDashboard(userId));
    }

    @GetMapping("/overview")
    public ResponseEntity<OverallAnalyticsResponse> getOverview(Authentication authentication) {
        Long userId = extractUserId(authentication);
        return ResponseEntity.ok(analyticsService.getOverallAnalytics(userId));
    }

    @GetMapping("/topics")
    public ResponseEntity<List<TopicAnalyticsResponse>> getTopics(Authentication authentication) {
        Long userId = extractUserId(authentication);
        return ResponseEntity.ok(analyticsService.getTopicAnalytics(userId));
    }

    @GetMapping("/topics/{topic}")
    public ResponseEntity<TopicAnalyticsResponse> getTopicAnalyticsByTopic(
            Authentication authentication,
            @PathVariable("topic") String topicStr) {
        Long userId = extractUserId(authentication);
        Topic topic = Topic.valueOf(topicStr.toUpperCase());
        return ResponseEntity.ok(analyticsService.getTopicAnalyticsByTopic(userId, topic));
    }

    @GetMapping("/difficulty")
    public ResponseEntity<List<DifficultyAnalyticsResponse>> getDifficultyAnalytics(Authentication authentication) {
        Long userId = extractUserId(authentication);
        return ResponseEntity.ok(analyticsService.getDifficultyAnalytics(userId));
    }

    @GetMapping("/trends")
    public ResponseEntity<PerformanceTrend> getTrends(Authentication authentication) {
        Long userId = extractUserId(authentication);
        return ResponseEntity.ok(analyticsService.getOverallTrend(userId));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<RecentActivityResponse>> getRecentActivity(
            Authentication authentication,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        Long userId = extractUserId(authentication);
        return ResponseEntity.ok(analyticsService.getRecentActivity(userId, limit));
    }
}