package com.samrat.ai.developer.assessment.assessment.repository;

import com.samrat.ai.developer.assessment.assessment.model.Assessment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AssessmentRepository extends JpaRepository<Assessment, Long> {
    Page<Assessment> findAllByActiveTrue(Pageable pageable);
    Optional<Assessment> findByIdAndActiveTrue(Long id);
}