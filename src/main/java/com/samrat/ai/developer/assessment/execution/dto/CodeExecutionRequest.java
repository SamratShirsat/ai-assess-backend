package com.samrat.ai.developer.assessment.execution.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CodeExecutionRequest {

    @NotNull(message = "Problem ID is required")
    private Long problemId;

    @NotBlank(message = "Language is required")
    private String language; // JAVA, PYTHON, JAVASCRIPT, CPP

    @NotBlank(message = "Source code cannot be empty")
    private String sourceCode;
}
