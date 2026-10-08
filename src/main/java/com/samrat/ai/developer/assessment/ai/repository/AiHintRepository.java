package com.samrat.ai.developer.assessment.ai.repository;

import com.samrat.ai.developer.assessment.ai.model.AiHint;
import com.samrat.ai.developer.assessment.ai.model.HintLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiHintRepository extends JpaRepository<AiHint, Long> {
    List<AiHint> findByUserIdAndProblemIdOrderByHintLevelAsc(Long userId, Long problemId);
    boolean existsByUserIdAndProblemIdAndHintLevel(Long userId, Long problemId, HintLevel hintLevel);
    long countByUserIdAndCreatedAtAfter(Long userId, java.time.OffsetDateTime after);
}
