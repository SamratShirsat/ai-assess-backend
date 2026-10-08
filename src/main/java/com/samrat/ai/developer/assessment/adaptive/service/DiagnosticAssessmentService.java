package com.samrat.ai.developer.assessment.adaptive.service;

import com.samrat.ai.developer.assessment.adaptive.config.AdaptiveProperties;
import com.samrat.ai.developer.assessment.adaptive.dto.AdaptiveDTOs.*;
import com.samrat.ai.developer.assessment.adaptive.model.DiagnosticAssessment;
import com.samrat.ai.developer.assessment.adaptive.model.DiagnosticAssessmentProblem;
import com.samrat.ai.developer.assessment.adaptive.model.DiagnosticAssessmentStatus;
import com.samrat.ai.developer.assessment.adaptive.repository.DiagnosticAssessmentRepository;
import com.samrat.ai.developer.assessment.problem.model.Problem;
import com.samrat.ai.developer.assessment.problem.repository.ProblemRepository;
import com.samrat.ai.developer.assessment.user.model.User;
import com.samrat.ai.developer.assessment.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiagnosticAssessmentService {

    private final DiagnosticAssessmentRepository diagnosticRepository;
    private final ProblemRepository problemRepository;
    private final UserRepository userRepository;
    private final AdaptiveProperties adaptiveProperties;

    @Transactional
    public DiagnosticAssessmentResponse startDiagnostic(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userEmail));

        DiagnosticAssessment diag = diagnosticRepository.findFirstByUserIdAndStatus(user.getId(), DiagnosticAssessmentStatus.IN_PROGRESS)
                .orElseGet(() -> {
                    List<Problem> sampleProblems = problemRepository.findAll().stream()
                            .limit(adaptiveProperties.getDiagnosticQuestionCount()).toList();

                    DiagnosticAssessment newDiag = DiagnosticAssessment.builder()
                            .user(user)
                            .status(DiagnosticAssessmentStatus.IN_PROGRESS)
                            .startedAt(LocalDateTime.now())
                            .build();

                    List<DiagnosticAssessmentProblem> daps = new ArrayList<>();
                    for (int i = 0; i < sampleProblems.size(); i++) {
                        daps.add(DiagnosticAssessmentProblem.builder()
                                .assessment(newDiag)
                                .problem(sampleProblems.get(i))
                                .sequenceNumber(i + 1)
                                .completed(false)
                                .build());
                    }
                    newDiag.setProblems(daps);
                    return diagnosticRepository.save(newDiag);
                });

        return mapToResponse(diag);
    }

    @Transactional(readOnly = true)
    public DiagnosticAssessmentResponse getCurrentDiagnostic(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userEmail));

        DiagnosticAssessment diag = diagnosticRepository.findTopByUserIdOrderByCreatedAtDesc(user.getId())
                .orElseThrow(() -> new IllegalStateException("No diagnostic assessment found."));

        return mapToResponse(diag);
    }

    private DiagnosticAssessmentResponse mapToResponse(DiagnosticAssessment diag) {
        List<DiagnosticProblemResponse> list = diag.getProblems().stream().map(dap -> {
            Problem p = dap.getProblem();
            return DiagnosticProblemResponse.builder()
                    .sequenceNumber(dap.getSequenceNumber())
                    .completed(dap.isCompleted())
                    .problem(MinimalProblemResponse.builder()
                            .id(p.getId())
                            .title(p.getTitle())
                            .difficulty(p.getDifficulty())
                            .topic(p.getTopic())
                            .description(p.getDescription())
                            .build())
                    .build();
        }).toList();

        return DiagnosticAssessmentResponse.builder()
                .id(diag.getId())
                .status(diag.getStatus())
                .currentQuestionIndex(diag.getCurrentQuestionIndex())
                .totalQuestions(diag.getProblems().size())
                .questions(list)
                .build();
    }
}
