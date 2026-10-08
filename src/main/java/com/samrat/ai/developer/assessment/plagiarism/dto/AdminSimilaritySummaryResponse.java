package com.samrat.ai.developer.assessment.plagiarism.dto;

import com.samrat.ai.developer.assessment.plagiarism.model.SimilarityConfidence;
import com.samrat.ai.developer.assessment.plagiarism.model.SimilarityLevel;
import com.samrat.ai.developer.assessment.plagiarism.model.SimilarityStatus;

import java.time.OffsetDateTime;

public record AdminSimilaritySummaryResponse(
        Long id,
        Long submissionAId,
        Long submissionBId,
        Long problemId,
        Double similarityScore,
        SimilarityLevel similarityLevel,
        SimilarityConfidence confidence,
        SimilarityStatus status,
        OffsetDateTime createdAt
) {}