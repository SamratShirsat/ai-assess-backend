package com.samrat.ai.developer.assessment.adaptive.dto;

import java.time.OffsetDateTime;

public record UserSkillProfileResponse(
        Long id,
        Long userId,
        Long problemId,
        String skillTag,
        Double proficiencyScore,
        Integer submissionsCount,
        Integer successfulSubmissionsCount,
        OffsetDateTime lastAssessedAt
) {}
