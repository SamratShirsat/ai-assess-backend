package com.samrat.ai.developer.assessment.assessment.repository;

import com.samrat.ai.developer.assessment.assessment.model.AssessmentSessionProblem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssessmentSessionProblemRepository extends JpaRepository<AssessmentSessionProblem, Long> {
    List<AssessmentSessionProblem> findAllBySessionIdOrderByAssessmentProblemDisplayOrderAsc(Long sessionId);

    @Query("SELECT asp FROM AssessmentSessionProblem asp WHERE asp.session.id = :sessionId AND asp.assessmentProblem.problem.id = :problemId")
    Optional<AssessmentSessionProblem> findBySessionIdAndProblemId(@Param("sessionId") Long sessionId, @Param("problemId") Long problemId);
}