package com.samrat.ai.developer.assessment.adaptive.repository;

import com.samrat.ai.developer.assessment.adaptive.model.AdaptiveRecommendation;
import com.samrat.ai.developer.assessment.adaptive.model.RecommendationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AdaptiveRecommendationRepository extends JpaRepository<AdaptiveRecommendation, Long> {
    Page<AdaptiveRecommendation> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Optional<AdaptiveRecommendation> findFirstByUserIdAndStatusOrderByCreatedAtDesc(Long userId, RecommendationStatus status);
    List<AdaptiveRecommendation> findByUserIdAndProblemId(Long userId, Long problemId);
}
