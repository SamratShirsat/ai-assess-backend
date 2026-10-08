package com.samrat.ai.developer.assessment.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssueResponse {
    private String severity; // HIGH, MEDIUM, LOW
    private String title;
    private String description;
    private String suggestion;
}