package com.samrat.ai.developer.assessment.testcase.repository;

import com.samrat.ai.developer.assessment.testcase.model.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TestCaseRepository extends JpaRepository<TestCase, Long> {
    List<TestCase> findByProblemIdOrderByIdAsc(Long problemId);
    List<TestCase> findByProblemIdAndSampleTrueOrderByIdAsc(Long problemId);
    long countByProblemId(Long problemId);
}