package com.samrat.ai.developer.assessment.execution.dto;

public enum ExecutionStatus {
    ACCEPTED,
    WRONG_ANSWER,
    SUCCESS,
    COMPILE_ERROR,
    RUNTIME_ERROR,
    TIME_LIMIT_EXCEEDED,
    MEMORY_LIMIT_EXCEEDED,
    OUTPUT_LIMIT_EXCEEDED,
    EXECUTION_ERROR,
    UNSUPPORTED_LANGUAGE
}
