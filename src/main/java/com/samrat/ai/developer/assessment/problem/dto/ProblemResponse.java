package com.samrat.ai.developer.assessment.problem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.samrat.ai.developer.assessment.problem.model.Difficulty;
import com.samrat.ai.developer.assessment.problem.model.Topic;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProblemResponse {

    private Long id;
    private String title;
    private String description;
    private Difficulty difficulty;
    private Topic topic;
    private String constraints;
    private String inputFormat;
    private String outputFormat;
    private String starterCode;
    private String expectedTimeComplexity;
    private String expectedSpaceComplexity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}