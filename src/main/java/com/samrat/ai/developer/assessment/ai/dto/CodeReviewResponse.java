package com.samrat.ai.developer.assessment.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeReviewResponse {
    private Long id;
    private Long submissionId;
    private Long problemId;
    private String summary;
    private CorrectnessResponse correctness;
    private ComplexityResponse complexity;
    private List<IssueResponse> issues;
    private List<String> edgeCases;
    private QualityResponse quality;
    private List<String> suggestions;
}