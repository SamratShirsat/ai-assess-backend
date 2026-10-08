package com.samrat.ai.developer.assessment.testcase.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

public class TestCaseDTOs {

    @Data
    public static class CreateRequest {
        @NotNull @Min(1)
        private Long problemId;
        @NotNull
        private String input;
        @NotNull
        private String expectedOutput;
        private boolean hidden;
        private boolean sample;
        @NotNull @Min(1)
        private Integer points;
    }

    @Data
    public static class UpdateRequest {
        @NotNull
        private String input;
        @NotNull
        private String expectedOutput;
        private boolean hidden;
        private boolean sample;
        @NotNull @Min(1)
        private Integer points;
    }

    @Data
    @Builder
    public static class PublicResponse {
        private Long id;
        private String input;
        private String expectedOutput;
        private boolean sample;
    }

    @Data
    @Builder
    public static class AdminResponse {
        private Long id;
        private Long problemId;
        private String input;
        private String expectedOutput;
        private boolean hidden;
        private boolean sample;
        private Integer points;
    }
}
