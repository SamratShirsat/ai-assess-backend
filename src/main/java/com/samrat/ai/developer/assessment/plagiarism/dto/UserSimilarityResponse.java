package com.samrat.ai.developer.assessment.plagiarism.dto;

import com.samrat.ai.developer.assessment.plagiarism.model.SimilarityLevel;

public record UserSimilarityResponse(
        Long submissionId,
        boolean potentiallySimilar,
        SimilarityLevel similarityLevel,
        String message
) {}