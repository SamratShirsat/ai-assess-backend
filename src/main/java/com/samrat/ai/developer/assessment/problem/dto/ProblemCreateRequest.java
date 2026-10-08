package com.samrat.ai.developer.assessment.problem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.samrat.ai.developer.assessment.problem.model.Difficulty;
import com.samrat.ai.developer.assessment.problem.model.Topic;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProblemCreateRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Difficulty is required")
    private Difficulty difficulty;

    @NotNull(message = "Topic is required")
    private Topic topic;

    private String constraints;
    private String inputFormat;
    private String outputFormat;
    private String starterCode;
    private String expectedTimeComplexity;
    private String expectedSpaceComplexity;
}