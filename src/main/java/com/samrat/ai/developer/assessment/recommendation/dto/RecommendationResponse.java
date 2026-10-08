package com.samrat.ai.developer.assessment.recommendation.dto;

import com.samrat.ai.developer.assessment.adaptive.model.RecommendationReasonType;

public class RecommendationResponse {

    private Long id;
    private Long problemId;
    private String title;
    private String topic;
    private String difficulty;
    private int skillScore;
    private double recommendationScore;

    // UPDATED: Now uses the unified Enum
    private RecommendationReasonType reasonType;
    private String reason;
    private Boolean previouslyAttempted;
    private Boolean previouslySolved;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProblemId() { return problemId; }
    public void setProblemId(Long problemId) { this.problemId = problemId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public int getSkillScore() { return skillScore; }
    public void setSkillScore(int skillScore) { this.skillScore = skillScore; }

    public double getRecommendationScore() { return recommendationScore; }
    public void setRecommendationScore(double recommendationScore) { this.recommendationScore = recommendationScore; }

    public RecommendationReasonType getReasonType() { return reasonType; }
    public void setReasonType(RecommendationReasonType reasonType) { this.reasonType = reasonType; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Boolean getPreviouslyAttempted() { return previouslyAttempted; }
    public void setPreviouslyAttempted(Boolean previouslyAttempted) { this.previouslyAttempted = previouslyAttempted; }

    public Boolean getPreviouslySolved() { return previouslySolved; }
    public void setPreviouslySolved(Boolean previouslySolved) { this.previouslySolved = previouslySolved; }
}