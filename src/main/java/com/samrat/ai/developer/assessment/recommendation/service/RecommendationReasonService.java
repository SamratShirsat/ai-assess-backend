package com.samrat.ai.developer.assessment.recommendation.service;

import com.samrat.ai.developer.assessment.problem.model.Problem;
import com.samrat.ai.developer.assessment.adaptive.model.RecommendationReasonType;
import org.springframework.stereotype.Service;

@Service
public class RecommendationReasonService {

    // UPDATED: The method now accepts RecommendationReasonType
    public String generateExplanation(Problem problem, RecommendationReasonType reasonType,
                                      double topicScore, double overallScore, int attempts) {

        if (reasonType == null) {
            return "Recommended based on your recent activity.";
        }

        switch (reasonType) {
            case WEAK_TOPIC:
            case WEAK_SKILL:
                return "Your performance in " + problem.getTopic() + " is slightly below your overall average. This will help strengthen your skills.";
            case UNDER_TESTED_TOPIC:
            case INSUFFICIENT_EVIDENCE:
                return "You haven't practiced " + problem.getTopic() + " much yet. Try this to establish your baseline.";
            case SKILL_REINFORCEMENT:
            case SKILL_BUILDING:
                return "This problem aligns perfectly with your current skill level to reinforce your knowledge.";
            case REVISION:
                return "You've successfully solved this before. Good for a quick revision!";
            case DIFFICULTY_RECOVERY:
                return "You attempted this recently without passing. Let's try again to conquer it!";
            case DIFFICULTY_PROMOTION:
            case DIFFICULTY_PROGRESSION:
                return "You are doing great! Here is a more challenging problem to push your limits.";
            case NEW_PROBLEM:
            case EXPLORATION:
                return "Here is something completely new to expand your skillset.";
            default:
                return "Recommended based on your recent activity and skill profile.";
        }
    }
}