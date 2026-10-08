package com.samrat.ai.developer.assessment.plagiarism.dto;

import com.samrat.ai.developer.assessment.plagiarism.model.SimilarityConfidence;
import com.samrat.ai.developer.assessment.plagiarism.model.SimilarityLevel;
import com.samrat.ai.developer.assessment.plagiarism.model.SimilarityStatus;

import java.time.OffsetDateTime;
import java.util.List;

public record AdminSimilarityDetailResponse(
        Long id,
        Long submissionAId,
        Long submissionBId,
        Long problemId,
        String problemTitle,
        String language,
        Double similarityScore,
        SimilarityLevel similarityLevel,
        SimilarityConfidence confidence,
        List<String> matchedFeatures,
        SimilarityStatus status,
        String sourceCodeA,
        String sourceCodeB,
        Long userAId,
        String userAUsername,
        Long userBId,
        String userBUsername,
        OffsetDateTime createdAt
) {}