package com.samrat.ai.developer.assessment.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.samrat.ai.developer.assessment.ai.dto.*;
import com.samrat.ai.developer.assessment.ai.exception.AiResponseParseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiResponseParser {

    private final ObjectMapper objectMapper;

    public CodeReviewResponse parseCodeReview(String rawResponse) {
        try {
            String cleanJson = extractJson(rawResponse);
            CodeReviewResponse response = objectMapper.readValue(cleanJson, CodeReviewResponse.class);

            // Sanitize & default values
            if (response.getSummary() == null) response.setSummary("Code review complete.");
            if (response.getCorrectness() == null) {
                response.setCorrectness(new CorrectnessResponse("UNKNOWN", "No correctness data provided."));
            }
            if (response.getComplexity() == null) {
                response.setComplexity(new ComplexityResponse("N/A", "N/A"));
            }
            if (response.getIssues() == null) response.setIssues(new ArrayList<>());
            if (response.getEdgeCases() == null) response.setEdgeCases(new ArrayList<>());
            if (response.getSuggestions() == null) response.setSuggestions(new ArrayList<>());

            if (response.getQuality() == null) {
                response.setQuality(new QualityResponse(5, 5));
            } else {
                // Clamp scores 1-10
                int r = Math.max(1, Math.min(10, response.getQuality().getReadability()));
                int m = Math.max(1, Math.min(10, response.getQuality().getMaintainability()));
                response.setQuality(new QualityResponse(r, m));
            }

            return response;

        } catch (Exception e) {
            log.error("Failed to parse AI code review JSON response: {}", e.getMessage());
            throw new AiResponseParseException("Failed to parse AI review output.", e);
        }
    }

    public String parseHint(String rawResponse) {
        try {
            String cleanJson = extractJson(rawResponse);
            var node = objectMapper.readTree(cleanJson);
            if (node.has("hint")) {
                return node.get("hint").asText();
            }
            throw new IllegalArgumentException("Missing 'hint' property in AI response");
        } catch (Exception e) {
            log.error("Failed to parse AI hint JSON response: {}", e.getMessage());
            throw new AiResponseParseException("Failed to parse AI hint output.", e);
        }
    }

    private String extractJson(String text) {
        if (text == null) return "{}";
        String trimmed = text.trim();
        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.substring(7);
        } else if (trimmed.startsWith("```")) {
            trimmed = trimmed.substring(3);
        }
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.length() - 3);
        }
        return trimmed.trim();
    }
}