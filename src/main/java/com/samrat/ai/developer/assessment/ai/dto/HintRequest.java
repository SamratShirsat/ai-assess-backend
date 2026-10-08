package com.samrat.ai.developer.assessment.ai.dto;

import com.samrat.ai.developer.assessment.ai.model.HintLevel;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class HintRequest {
    @NotNull(message = "Problem ID is required")
    private Long problemId;

    private String language;

    @Size(max = 30000, message = "Source code exceeds limit")
    private String sourceCode;

    @NotNull(message = "Hint level is required")
    private HintLevel hintLevel;
}