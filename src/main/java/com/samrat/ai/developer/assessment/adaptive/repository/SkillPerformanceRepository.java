package com.samrat.ai.developer.assessment.adaptive.repository;

import com.samrat.ai.developer.assessment.adaptive.model.SkillPerformance;
import com.samrat.ai.developer.assessment.problem.model.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SkillPerformanceRepository extends JpaRepository<SkillPerformance, Long> {
    Optional<SkillPerformance> findByUserSkillProfileIdAndTopic(Long profileId, Topic topic);
    List<SkillPerformance> findByUserSkillProfileId(Long profileId);

    @Query("SELECT sp FROM SkillPerformance sp WHERE sp.userSkillProfile.user.id = :userId")
    List<SkillPerformance> findByUserId(@Param("userId") Long userId);

    List<SkillPerformance> findAllByUserSkillProfileUserId(Long userId);
}