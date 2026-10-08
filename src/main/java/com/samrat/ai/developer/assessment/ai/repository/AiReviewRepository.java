package com.samrat.ai.developer.assessment.ai.repository;

import com.samrat.ai.developer.assessment.ai.model.AiReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AiReviewRepository extends JpaRepository<AiReview, Long> {
    List<AiReview> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<AiReview> findByUserIdAndSubmissionId(Long userId, Long submissionId);
    Optional<AiReview> findByIdAndUserId(Long id, Long userId);
}