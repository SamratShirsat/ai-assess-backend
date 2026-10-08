package com.samrat.ai.developer.assessment.recommendation.service;

import com.samrat.ai.developer.assessment.problem.model.Problem;
import com.samrat.ai.developer.assessment.adaptive.model.RecommendationReasonType;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RecommendationScoringService {

    public EvaluationResult evaluateCandidate(
            Problem problem, double overallScore, Map<String, Double> topicScores,
            Map<String, Integer> topicAttempts, boolean isAttempted, boolean isSolved, boolean isRecent) {

        double score = 50.0;
        RecommendationReasonType reason = RecommendationReasonType.SKILL_BUILDING;

        String topic = problem.getTopic() != null ? problem.getTopic().toString() : "GENERAL";
        double topicScore = topicScores.getOrDefault(topic, overallScore);
        int attempts = topicAttempts.getOrDefault(topic, 0);

        if (isSolved) {
            score = 10.0;
            reason = RecommendationReasonType.REVISION;
        } else if (topicScore < overallScore - 10) {
            score = 90.0;
            reason = RecommendationReasonType.WEAK_TOPIC;
        } else if (attempts < 2) {
            score = 80.0;
            reason = RecommendationReasonType.UNDER_TESTED_TOPIC;
        } else if (isAttempted && !isSolved) {
            score = 75.0;
            reason = RecommendationReasonType.DIFFICULTY_RECOVERY;
        } else {
            score = 60.0;
            reason = RecommendationReasonType.SKILL_REINFORCEMENT;
        }

        return new EvaluationResult(score, reason);
    }

    // UPDATED: This class now expects RecommendationReasonType
    public static class EvaluationResult {
        public double totalScore;
        public RecommendationReasonType primaryReason;

        public EvaluationResult(double totalScore, RecommendationReasonType primaryReason) {
            this.totalScore = totalScore;
            this.primaryReason = primaryReason;
        }
    }
}