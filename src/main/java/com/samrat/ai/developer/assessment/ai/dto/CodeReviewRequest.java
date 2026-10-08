package com.samrat.ai.developer.assessment.ai.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CodeReviewRequest {
    private Long submissionId;
    private Long problemId;
    private String language;

    @Size(max = 30000, message = "Source code exceeds maximum input character limit.")
    private String sourceCode;
}