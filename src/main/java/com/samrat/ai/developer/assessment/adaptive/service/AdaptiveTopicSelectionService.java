package com.samrat.ai.developer.assessment.adaptive.service;

import com.samrat.ai.developer.assessment.adaptive.config.AdaptiveProperties;
import com.samrat.ai.developer.assessment.adaptive.model.SkillPerformance;
import com.samrat.ai.developer.assessment.adaptive.model.UserSkillProfile;
import com.samrat.ai.developer.assessment.problem.model.Topic;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AdaptiveTopicSelectionService {

    private final AdaptiveProperties adaptiveProperties;

    public Topic selectNextTopic(UserSkillProfile profile, List<SkillPerformance> performances) {
        Map<Topic, SkillPerformance> performanceMap = new EnumMap<>(Topic.class);
        if (performances != null) {
            performances.forEach(sp -> performanceMap.put(sp.getTopic(), sp));
        }

        // Check for unexplored topics (Lack of Evidence)
        List<Topic> unexplored = new ArrayList<>();
        for (Topic t : Topic.values()) {
            if (!performanceMap.containsKey(t) || performanceMap.get(t).getTotalAttempts() == 0) {
                unexplored.add(t);
            }
        }

        // Exploitation vs Exploration decision
        boolean explore = Math.random() < adaptiveProperties.getExplorationRatio();

        if (explore && !unexplored.isEmpty()) {
            Collections.shuffle(unexplored);
            return unexplored.get(0); // Select random unexplored topic
        }

        // Priority Score Ranking
        Topic bestTopic = Topic.ARRAY;
        double maxPriority = -1.0;

        for (Topic t : Topic.values()) {
            SkillPerformance sp = performanceMap.get(t);
            double priority = calculateTopicPriority(sp);
            if (priority > maxPriority) {
                maxPriority = priority;
                bestTopic = t;
            }
        }

        return bestTopic;
    }

    private double calculateTopicPriority(SkillPerformance sp) {
        if (sp == null || sp.getTotalAttempts() == 0) {
            return 1.0; // High priority for unattempted
        }

        double weaknessScore = 1.0 - (sp.getSkillScore() / 100.0);
        double lackOfEvidenceScore = Math.max(0.0, 1.0 - (sp.getTotalAttempts() / 10.0));

        double recencyScore = 0.5;
        if (sp.getLastAttemptAt() != null) {
            long daysSince = Duration.between(sp.getLastAttemptAt(), LocalDateTime.now()).toDays();
            recencyScore = Math.min(1.0, daysSince / 14.0);
        }

        // Weighted priority
        return (0.50 * weaknessScore) + (0.25 * lackOfEvidenceScore) + (0.25 * recencyScore);
    }
}
