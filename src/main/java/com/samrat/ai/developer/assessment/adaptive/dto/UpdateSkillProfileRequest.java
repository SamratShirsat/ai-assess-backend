package com.samrat.ai.developer.assessment.adaptive.dto;

public record UpdateSkillProfileRequest(
        Long problemId,
        String skillTag,
        Double scoreIncrement,
        boolean isSuccess
) {}