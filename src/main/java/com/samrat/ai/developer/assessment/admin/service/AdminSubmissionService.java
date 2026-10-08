// AdminSubmissionService.java
package com.samrat.ai.developer.assessment.admin.service;

import com.samrat.ai.developer.assessment.admin.dto.AdminDTOs.*;
import com.samrat.ai.developer.assessment.submission.model.Submission;
import com.samrat.ai.developer.assessment.submission.model.SubmissionStatus;
import com.samrat.ai.developer.assessment.submission.repository.SubmissionRepository;
import com.samrat.ai.developer.assessment.execution.model.SupportedLanguage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AdminSubmissionService {

    private final SubmissionRepository submissionRepository;

    @Transactional(readOnly = true)
    public Page<AdminSubmissionOverview> getSubmissions(SubmissionStatus status, String language, Pageable pageable) {
        Page<Submission> subs;
        if (status != null) {
            subs = submissionRepository.findAllByStatus(status, pageable);
        } else if (language != null && !language.isBlank()) {
            subs = submissionRepository.findAllByLanguage(SupportedLanguage.valueOf(language.toUpperCase()), pageable);
        } else {
            subs = submissionRepository.findAll(pageable);
        }

        return subs.map(s -> new AdminSubmissionOverview(
                s.getId(),
                s.getUser().getId(),
                s.getUser().getName(),
                s.getProblem().getId(),
                s.getProblem().getTitle(),
                s.getLanguage().name(),
                s.getStatus(),
                BigDecimal.valueOf(s.getScore() != null ? s.getScore() : 0),
                s.getPassedTests(),
                s.getTotalTests(),
                s.getExecutionTimeMs(),
                s.getSubmittedAt()
        ));
    }

    @Transactional(readOnly = true)
    public AdminSubmissionDetail getSubmissionDetails(Long id) {
        Submission s = submissionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found: " + id));

        return new AdminSubmissionDetail(
                s.getId(),
                s.getUser().getId(),
                s.getUser().getName(),
                s.getUser().getEmail(),
                s.getProblem().getId(),
                s.getProblem().getTitle(),
                s.getLanguage().name(),
                s.getSourceCode(),
                s.getStatus(),
                BigDecimal.valueOf(s.getScore() != null ? s.getScore() : 0),
                s.getPassedTests(),
                s.getTotalTests(),
                s.getExecutionTimeMs(),
                s.getSubmittedAt()
        );
    }
}
