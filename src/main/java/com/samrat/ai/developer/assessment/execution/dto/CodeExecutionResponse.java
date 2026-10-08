package com.samrat.ai.developer.assessment.execution.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeExecutionResponse {

    private ExecutionStatus status;
    private String compilerOutput;
    private int totalTestCases;
    private int passedTestCases;
    private List<TestCaseResult> testResults;
    private long maxExecutionTimeMs;
    private String stdout;
    private String stderr;
    private long executionTimeMs;
    private Integer exitCode;
    private String message;
}
