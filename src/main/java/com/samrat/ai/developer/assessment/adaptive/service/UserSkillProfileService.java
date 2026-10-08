package com.samrat.ai.developer.assessment.adaptive.service;

import com.samrat.ai.developer.assessment.adaptive.model.UserSkillProfile;
import com.samrat.ai.developer.assessment.adaptive.repository.SkillPerformanceRepository;
import com.samrat.ai.developer.assessment.adaptive.repository.UserSkillProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserSkillProfileService {

    private final UserSkillProfileRepository profileRepository;
    private final SkillPerformanceRepository performanceRepository;

    public double getOverallSkillScore(Long userId) {
        return profileRepository.findByUserId(userId)
                .map(UserSkillProfile::getOverallScore)
                .orElse(0.0);
    }

    public Map<String, Double> getTopicSkillScores(Long userId) {
        return performanceRepository.findAllByUserSkillProfileUserId(userId).stream()
                .filter(p -> p.getTopic() != null && p.getSkillScore() != null)
                .collect(Collectors.toMap(p -> p.getTopic().name(), p -> p.getSkillScore().doubleValue(), (a, b) -> b));
    }

    public Map<String, Integer> getTopicAttemptCounts(Long userId) {
        return performanceRepository.findAllByUserSkillProfileUserId(userId).stream()
                .filter(p -> p.getTopic() != null)
                .collect(Collectors.toMap(p -> p.getTopic().name(), p -> p.getTotalAttempts() == null ? 0 : p.getTotalAttempts(), (a, b) -> b));
    }
}
