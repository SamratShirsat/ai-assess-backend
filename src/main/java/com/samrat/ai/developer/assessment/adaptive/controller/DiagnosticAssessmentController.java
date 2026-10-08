package com.samrat.ai.developer.assessment.adaptive.controller;

import com.samrat.ai.developer.assessment.adaptive.dto.AdaptiveDTOs.DiagnosticAssessmentResponse;
import com.samrat.ai.developer.assessment.adaptive.service.DiagnosticAssessmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assessment/diagnostic")
@RequiredArgsConstructor
public class DiagnosticAssessmentController {

    private final DiagnosticAssessmentService diagnosticService;

    @PostMapping("/start")
    public ResponseEntity<DiagnosticAssessmentResponse> startDiagnostic(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(diagnosticService.startDiagnostic(userDetails.getUsername()));
    }

    @GetMapping("/current")
    public ResponseEntity<DiagnosticAssessmentResponse> getCurrentDiagnostic(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(diagnosticService.getCurrentDiagnostic(userDetails.getUsername()));
    }
}
