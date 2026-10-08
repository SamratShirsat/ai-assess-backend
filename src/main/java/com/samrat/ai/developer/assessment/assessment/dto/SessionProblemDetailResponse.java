package com.samrat.ai.developer.assessment.assessment.dto;

import com.samrat.ai.developer.assessment.assessment.model.AssessmentProblemStatus;
import com.samrat.ai.developer.assessment.problem.model.Difficulty;
import com.samrat.ai.developer.assessment.problem.model.Topic;

public record SessionProblemDetailResponse(
        Long problemId,
        String title,
        String description,
        Difficulty difficulty,
        Topic topic,
        String constraints,
        String inputFormat,
        String outputFormat,
        String starterCode,
        Integer points,
        Integer displayOrder,
        AssessmentProblemStatus status,
        String lastSavedCode,
        String lastSavedLanguage,
        Integer attemptCount
) {}