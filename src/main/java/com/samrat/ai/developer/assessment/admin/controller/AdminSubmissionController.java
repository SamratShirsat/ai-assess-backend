package com.samrat.ai.developer.assessment.admin.controller;

import com.samrat.ai.developer.assessment.admin.dto.AdminDTOs.*;
import com.samrat.ai.developer.assessment.admin.service.AdminSubmissionService;
import com.samrat.ai.developer.assessment.submission.model.SubmissionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/submissions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminSubmissionController {

    private final AdminSubmissionService submissionService;

    @GetMapping
    public ResponseEntity<Page<AdminSubmissionOverview>> getSubmissions(
            @RequestParam(required = false) SubmissionStatus status,
            @RequestParam(required = false) String language,
            Pageable pageable) {
        return ResponseEntity.ok(submissionService.getSubmissions(status, language, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminSubmissionDetail> getSubmission(@PathVariable Long id) {
        return ResponseEntity.ok(submissionService.getSubmissionDetails(id));
    }
}