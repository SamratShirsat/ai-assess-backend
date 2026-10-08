package com.samrat.ai.developer.assessment.assessment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DraftSaveRequest(
        @NotBlank @Size(max = 100000) String sourceCode,
        @NotBlank String language
) {}