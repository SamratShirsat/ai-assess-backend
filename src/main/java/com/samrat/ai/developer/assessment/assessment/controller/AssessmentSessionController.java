package com.samrat.ai.developer.assessment.assessment.controller;

import com.samrat.ai.developer.assessment.assessment.dto.*;
import com.samrat.ai.developer.assessment.assessment.service.AssessmentSessionService;
import com.samrat.ai.developer.assessment.security.UserPrincipal;
import com.samrat.ai.developer.assessment.submission.dto.SubmissionDTOs.CodeSubmissionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AssessmentSessionController {

    private final AssessmentSessionService sessionService;

    @PostMapping("/assessments/{assessmentId}/start")
    public ResponseEntity<StartAssessmentResponse> startAssessment(
            @PathVariable Long assessmentId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return ResponseEntity.ok(sessionService.startAssessment(assessmentId, userPrincipal.getId()));
    }

    @GetMapping("/assessment-sessions/{sessionId}/time")
    public ResponseEntity<AssessmentTimeResponse> getSessionTime(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return ResponseEntity.ok(sessionService.getSessionTime(sessionId, userPrincipal.getId()));
    }

    @PutMapping("/assessment-sessions/{sessionId}/problems/{problemId}/draft")
    public ResponseEntity<Void> saveDraft(
            @PathVariable Long sessionId,
            @PathVariable Long problemId,
            @Valid @RequestBody DraftSaveRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        sessionService.saveDraft(sessionId, problemId,request, userPrincipal.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/assessment-sessions/{sessionId}/problems/{problemId}/submit")
    public ResponseEntity<CodeSubmissionResponse> submitProblem(
            @PathVariable Long sessionId,
            @PathVariable Long problemId,
            @Valid @RequestBody AssessmentSubmissionRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return ResponseEntity.ok(sessionService.processAssessmentSubmission(sessionId, problemId, userPrincipal.getId(), request));
    }

    @PostMapping("/assessment-sessions/{sessionId}/finish")
    public ResponseEntity<AssessmentResultResponse> finishAssessment(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return ResponseEntity.ok(sessionService.finishAssessment(sessionId, userPrincipal.getId()));
    }
}
