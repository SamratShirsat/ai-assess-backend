package com.samrat.ai.developer.assessment.adaptive.controller;

import com.samrat.ai.developer.assessment.adaptive.dto.AdaptiveDTOs.AdaptiveRecommendationResponse;
import com.samrat.ai.developer.assessment.adaptive.service.AdaptiveAssessmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/adaptive")
@RequiredArgsConstructor
public class AdaptiveController {

    private final AdaptiveAssessmentService adaptiveAssessmentService;

    @GetMapping("/next-problem")
    public ResponseEntity<AdaptiveRecommendationResponse> getNextAdaptiveProblem(@AuthenticationPrincipal UserDetails userDetails) {
        AdaptiveRecommendationResponse response = adaptiveAssessmentService.getNextProblem(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }
}
