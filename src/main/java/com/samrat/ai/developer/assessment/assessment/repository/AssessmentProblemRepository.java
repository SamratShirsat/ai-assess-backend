package com.samrat.ai.developer.assessment.assessment.repository;

import com.samrat.ai.developer.assessment.assessment.model.AssessmentProblem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssessmentProblemRepository extends JpaRepository<AssessmentProblem, Long> {
    List<AssessmentProblem> findAllByAssessmentIdOrderByDisplayOrderAsc(Long assessmentId);
    Optional<AssessmentProblem> findByAssessmentIdAndProblemId(Long assessmentId, Long problemId);
    boolean existsByAssessmentIdAndProblemId(Long assessmentId, Long problemId);
}