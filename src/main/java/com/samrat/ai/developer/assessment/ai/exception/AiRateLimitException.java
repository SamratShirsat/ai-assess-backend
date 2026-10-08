package com.samrat.ai.developer.assessment.ai.exception;

public class AiRateLimitException extends RuntimeException {
    public AiRateLimitException(String message) {
        super(message);
    }
}