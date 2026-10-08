package com.samrat.ai.developer.assessment.adaptive.repository;

import com.samrat.ai.developer.assessment.adaptive.model.DiagnosticAssessment;
import com.samrat.ai.developer.assessment.adaptive.model.DiagnosticAssessmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DiagnosticAssessmentRepository extends JpaRepository<DiagnosticAssessment, Long> {
    Optional<DiagnosticAssessment> findFirstByUserIdAndStatus(Long userId, DiagnosticAssessmentStatus status);
    Optional<DiagnosticAssessment> findTopByUserIdOrderByCreatedAtDesc(Long userId);
}
