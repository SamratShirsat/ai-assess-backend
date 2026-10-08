package com.samrat.ai.developer.assessment.exception;

public class ExecutionExceptions {
    public static class CodeExecutionException extends RuntimeException {
        public CodeExecutionException(String message) { super(message); }
    }

    public static class ExecutionTimeoutException extends RuntimeException {
        public ExecutionTimeoutException(String message) { super(message); }
    }

    public static class UnsupportedLanguageException extends RuntimeException {
        public UnsupportedLanguageException(String message) { super(message); }
    }

    public static class RateLimitExceededException extends RuntimeException {
        public RateLimitExceededException(String message) { super(message); }
    }
}