package com.samrat.ai.developer.assessment.recommendation.dto;

import com.samrat.ai.developer.assessment.recommendation.model.RecommendationFeedbackType;
import jakarta.validation.constraints.NotNull;

public class RecommendationFeedbackRequest {

    @NotNull(message = "Feedback type is required")
    private RecommendationFeedbackType feedback;

    public RecommendationFeedbackRequest() {}

    public RecommendationFeedbackRequest(RecommendationFeedbackType feedback) {
        this.feedback = feedback;
    }

    public RecommendationFeedbackType getFeedback() { return feedback; }
    public void setFeedback(RecommendationFeedbackType feedback) { this.feedback = feedback; }
}