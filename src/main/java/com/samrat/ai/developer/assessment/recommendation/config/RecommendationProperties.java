package com.samrat.ai.developer.assessment.recommendation.config;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "recommendation")
public class RecommendationProperties {

    private int recentAttemptWindow = 10;
    private double exploitationRatio = 0.70;
    private double explorationRatio = 0.30;
    private Weights weights = new Weights();

    public static class Weights {
        private double topicRelevance = 0.35;
        private double difficultySuitability = 0.25;
        private double learningProgression = 0.20;
        private double novelty = 0.10;
        private double skillCoverage = 0.10;

        public double getTopicRelevance() { return topicRelevance; }
        public void setTopicRelevance(double topicRelevance) { this.topicRelevance = topicRelevance; }

        public double getDifficultySuitability() { return difficultySuitability; }
        public void setDifficultySuitability(double difficultySuitability) { this.difficultySuitability = difficultySuitability; }

        public double getLearningProgression() { return learningProgression; }
        public void setLearningProgression(double learningProgression) { this.learningProgression = learningProgression; }

        public double getNovelty() { return novelty; }
        public void setNovelty(double novelty) { this.novelty = novelty; }

        public double getSkillCoverage() { return skillCoverage; }
        public void setSkillCoverage(double skillCoverage) { this.skillCoverage = skillCoverage; }
    }

    @PostConstruct
    public void validateWeights() {
        double sum = weights.getTopicRelevance() + weights.getDifficultySuitability()
                + weights.getLearningProgression() + weights.getNovelty()
                + weights.getSkillCoverage();
        if (Math.abs(sum - 1.0) > 0.001) {
            throw new IllegalStateException("Recommendation weights must sum to 1.0. Current sum: " + sum);
        }
    }

    public int getRecentAttemptWindow() { return recentAttemptWindow; }
    public void setRecentAttemptWindow(int recentAttemptWindow) { this.recentAttemptWindow = recentAttemptWindow; }

    public double getExploitationRatio() { return exploitationRatio; }
    public void setExploitationRatio(double exploitationRatio) { this.exploitationRatio = exploitationRatio; }

    public double getExplorationRatio() { return explorationRatio; }
    public void setExplorationRatio(double explorationRatio) { this.explorationRatio = explorationRatio; }

    public Weights getWeights() { return weights; }
    public void setWeights(Weights weights) { this.weights = weights; }
}