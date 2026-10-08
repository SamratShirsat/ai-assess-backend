package com.samrat.ai.developer.assessment.ai.controller;

import com.samrat.ai.developer.assessment.ai.dto.*;
import com.samrat.ai.developer.assessment.ai.service.AiCodeReviewService;
import com.samrat.ai.developer.assessment.ai.service.AiHintService;
import com.samrat.ai.developer.assessment.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiCodeReviewService aiCodeReviewService;
    private final AiHintService aiHintService;

    @PostMapping("/review")
    public ResponseEntity<CodeReviewResponse> reviewCode(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody CodeReviewRequest request) {
        return ResponseEntity.ok(aiCodeReviewService.reviewCode(userPrincipal.getId(), request));
    }

    @PostMapping("/hint")
    public ResponseEntity<HintResponse> getHint(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody HintRequest request) {
        return ResponseEntity.ok(aiHintService.getHint(userPrincipal.getId(), request));
    }

    @GetMapping("/reviews")
    public ResponseEntity<List<CodeReviewResponse>> getUserReviews(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(aiCodeReviewService.getUserReviews(userPrincipal.getId()));
    }

    @GetMapping("/reviews/{id}")
    public ResponseEntity<CodeReviewResponse> getReviewById(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {
        return ResponseEntity.ok(aiCodeReviewService.getReviewById(userPrincipal.getId(), id));
    }
}