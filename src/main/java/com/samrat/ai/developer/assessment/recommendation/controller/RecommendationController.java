package com.samrat.ai.developer.assessment.recommendation.controller;

import com.samrat.ai.developer.assessment.recommendation.dto.RecommendationFeedbackRequest;
import com.samrat.ai.developer.assessment.recommendation.dto.RecommendationFeedbackResponse;
import com.samrat.ai.developer.assessment.recommendation.dto.RecommendationResponse;
import com.samrat.ai.developer.assessment.recommendation.service.RecommendationService;
import com.samrat.ai.developer.assessment.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping
    public ResponseEntity<List<RecommendationResponse>> getRecommendations(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "5") int limit,
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) String difficulty
    ) {
        List<RecommendationResponse> recommendations = recommendationService.getPersonalizedRecommendations(
                principal.getId(), limit, topic, difficulty
        );
        return ResponseEntity.ok(recommendations);
    }

    @GetMapping("/next")
    public ResponseEntity<RecommendationResponse> getNextRecommendation(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        RecommendationResponse nextProblem = recommendationService.getNextRecommendation(principal.getId());
        if (nextProblem == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(nextProblem);
    }

    @GetMapping("/history")
    public ResponseEntity<Page<RecommendationResponse>> getRecommendationHistory(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<RecommendationResponse> history = recommendationService.getRecommendationHistory(
                principal.getId(), page, size
        );
        return ResponseEntity.ok(history);
    }

    @PostMapping("/{id}/feedback")
    public ResponseEntity<RecommendationFeedbackResponse> submitFeedback(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody RecommendationFeedbackRequest request
    ) {
        RecommendationFeedbackResponse response = recommendationService.submitFeedback(
                principal.getId(), id, request
        );
        return ResponseEntity.ok(response);
    }
}