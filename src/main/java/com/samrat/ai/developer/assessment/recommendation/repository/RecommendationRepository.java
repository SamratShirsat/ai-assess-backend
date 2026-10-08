package com.samrat.ai.developer.assessment.recommendation.repository;

import com.samrat.ai.developer.assessment.adaptive.model.AdaptiveRecommendation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RecommendationRepository extends JpaRepository<AdaptiveRecommendation, Long> {

    Page<AdaptiveRecommendation> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Optional<AdaptiveRecommendation> findByIdAndUserId(Long id, Long userId);
}