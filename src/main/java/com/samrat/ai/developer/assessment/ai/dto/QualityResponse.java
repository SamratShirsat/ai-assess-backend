package com.samrat.ai.developer.assessment.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QualityResponse {
    private int readability; // 1-10
    private int maintainability; // 1-10
}