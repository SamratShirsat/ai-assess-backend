package com.samrat.ai.developer.assessment.recommendation.dto;

import com.samrat.ai.developer.assessment.recommendation.model.RecommendationFeedbackType;

public class RecommendationFeedbackResponse {

    private Long recommendationId;
    private RecommendationFeedbackType feedback;
    private String message;

    public RecommendationFeedbackResponse() {}

    public RecommendationFeedbackResponse(Long recommendationId, RecommendationFeedbackType feedback, String message) {
        this.recommendationId = recommendationId;
        this.feedback = feedback;
        this.message = message;
    }

    public Long getRecommendationId() { return recommendationId; }
    public void setRecommendationId(Long recommendationId) { this.recommendationId = recommendationId; }

    public RecommendationFeedbackType getFeedback() { return feedback; }
    public void setFeedback(RecommendationFeedbackType feedback) { this.feedback = feedback; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}