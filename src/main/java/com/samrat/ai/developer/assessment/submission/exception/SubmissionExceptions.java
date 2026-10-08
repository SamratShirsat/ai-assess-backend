package com.samrat.ai.developer.assessment.submission.exception;

public final class SubmissionExceptions {
    private SubmissionExceptions() {}

    public static class RateLimitExceededException extends RuntimeException {
        public RateLimitExceededException(String message) { super(message); }
    }

    public static class SubmissionNotFoundException extends RuntimeException {
        public SubmissionNotFoundException(String message) { super(message); }
    }
}
