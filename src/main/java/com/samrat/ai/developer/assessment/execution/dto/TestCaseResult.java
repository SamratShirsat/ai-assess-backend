package com.samrat.ai.developer.assessment.execution.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestCaseResult {
    private Long testCaseId;
    private boolean passed;
    private String input;
    private String expectedOutput;
    private String actualOutput;
    private String errorMessage;
    private long executionTimeMs;
}