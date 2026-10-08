package com.samrat.ai.developer.assessment.submission.service;

import com.samrat.ai.developer.assessment.submission.dto.SubmissionDTOs;
import com.samrat.ai.developer.assessment.submission.model.Submission;
import com.samrat.ai.developer.assessment.user.model.User;
import com.samrat.ai.developer.assessment.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import com.samrat.ai.developer.assessment.submission.exception.SubmissionExceptions;
import com.samrat.ai.developer.assessment.submission.repository.SubmissionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubmissionQueryService {

    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<SubmissionDTOs.CodeSubmissionResponse> getUserSubmissions(String userEmail, Long problemId, Pageable pageable) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userEmail));

        Page<Submission> page = (problemId != null)
                ? submissionRepository.findByUserIdAndProblemIdOrderBySubmittedAtDesc(user.getId(), problemId, pageable)
                : submissionRepository.findByUserIdOrderBySubmittedAtDesc(user.getId(), pageable);

        return page.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public SubmissionDTOs.CodeSubmissionResponse getSubmissionById(Long submissionId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userEmail));

        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new SubmissionExceptions.SubmissionNotFoundException("Submission not found with ID: " + submissionId));

        if (!submission.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Access denied to submission ID: " + submissionId);
        }

        return mapToResponse(submission);
    }

    private SubmissionDTOs.CodeSubmissionResponse mapToResponse(Submission sub) {
        return SubmissionDTOs.CodeSubmissionResponse.builder()
                .submissionId(sub.getId())
                .problemId(sub.getProblem().getId())
                .problemTitle(sub.getProblem().getTitle())
                .language(sub.getLanguage())
                .status(sub.getStatus())
                .score(sub.getScore())
                .totalPoints(sub.getTotalPoints())
                .passedTests(sub.getPassedTests())
                .totalTests(sub.getTotalTests())
                .executionTimeMs(sub.getExecutionTimeMs())
                .submittedAt(sub.getSubmittedAt())
                .build();
    }
}
