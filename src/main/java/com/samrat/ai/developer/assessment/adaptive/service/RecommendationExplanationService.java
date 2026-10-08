package com.samrat.ai.developer.assessment.adaptive.service;

import com.samrat.ai.developer.assessment.adaptive.model.SkillPerformance;
import com.samrat.ai.developer.assessment.adaptive.model.RecommendationReasonType;
import com.samrat.ai.developer.assessment.problem.model.Difficulty;
import com.samrat.ai.developer.assessment.problem.model.Topic;
import org.springframework.stereotype.Service;

@Service
public class RecommendationExplanationService {

    public String generateExplanation(RecommendationReasonType reasonType, Topic topic, Difficulty difficulty, SkillPerformance sp) {
        return switch (reasonType) {
            case WEAK_SKILL -> String.format(
                    "Your %s performance is currently below your target. We selected an %s %s problem to strengthen your core skills.",
                    topic, difficulty, topic);
            case DIFFICULTY_PROMOTION -> String.format(
                    "You've shown strong mastery in recent attempts! We've promoted your difficulty to %s for %s.",
                    difficulty, topic);
            case DIFFICULTY_RECOVERY -> String.format(
                    "Let's rebuild confidence in %s. We've adjusted difficulty to %s to help reinforce key concepts.",
                    topic, difficulty);
            case INSUFFICIENT_EVIDENCE -> String.format(
                    "We need more data to accurately evaluate your %s proficiency. Try this %s problem.",
                    topic, difficulty);
            case EXPLORATION -> String.format(
                    "Time to explore new areas! Testing your skills in %s at %s level.",
                    topic, difficulty);
            case DIAGNOSTIC -> String.format(
                    "Diagnostic Assessment Question: Evaluating initial skills in %s.", topic);
            default -> String.format("Recommended %s problem in %s based on your overall learning trajectory.", difficulty, topic);
        };
    }
}
