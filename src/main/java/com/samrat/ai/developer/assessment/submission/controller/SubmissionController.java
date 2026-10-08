package com.samrat.ai.developer.assessment.submission.controller;

import com.samrat.ai.developer.assessment.submission.dto.SubmissionDTOs;
import com.samrat.ai.developer.assessment.submission.service.SubmissionEvaluationService;
import com.samrat.ai.developer.assessment.submission.service.SubmissionQueryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionEvaluationService evaluationService;
    private final SubmissionQueryService queryService;

    @PostMapping
    public ResponseEntity<SubmissionDTOs.CodeSubmissionResponse> submitCode(
            @Valid @RequestBody SubmissionDTOs.CodeSubmissionRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        SubmissionDTOs.CodeSubmissionResponse response = evaluationService.evaluate(request, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<SubmissionDTOs.CodeSubmissionResponse>> getUserSubmissions(
            @RequestParam(required = false) Long problemId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Page<SubmissionDTOs.CodeSubmissionResponse> result = queryService.getUserSubmissions(
                userDetails.getUsername(),
                problemId,
                PageRequest.of(page, size)
        );
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubmissionDTOs.CodeSubmissionResponse> getSubmissionById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        SubmissionDTOs.CodeSubmissionResponse response = queryService.getSubmissionById(id, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }
}