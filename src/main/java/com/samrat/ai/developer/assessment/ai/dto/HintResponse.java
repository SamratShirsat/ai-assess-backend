package com.samrat.ai.developer.assessment.ai.dto;

import com.samrat.ai.developer.assessment.ai.model.HintLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HintResponse {
    private HintLevel hintLevel;
    private String hint;
    private Long problemId;
}