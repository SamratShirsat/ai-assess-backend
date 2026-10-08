package com.samrat.ai.developer.assessment.ai.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.samrat.ai.developer.assessment.ai.config.AiProperties;
import com.samrat.ai.developer.assessment.ai.exception.AiServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class LlmAiClient implements AiClient {

    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper;

    @Override
    public String generate(String systemPrompt, String userPrompt) {
        if (!aiProperties.isEnabled()) {
            throw new AiServiceException("AI service is currently disabled by configuration.");
        }

        if (aiProperties.getApiKey() == null || aiProperties.getApiKey().isBlank()) {
            throw new AiServiceException("AI API Key is missing on the server.");
        }

        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(aiProperties.getApiKey());

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", aiProperties.getModel());
            requestBody.put("max_tokens", aiProperties.getMaxOutputTokens());

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemPrompt));
            messages.add(Map.of("role", "user", "content", userPrompt));
            requestBody.put("messages", messages);

            // Response format hint for structured output where supported
            if ("openai".equalsIgnoreCase(aiProperties.getProvider())) {
                requestBody.put("response_format", Map.of("type", "json_object"));
            }

            String jsonPayload = objectMapper.writeValueAsString(requestBody);
            HttpEntity<String> entity = new HttpEntity<>(jsonPayload, headers);

            String endpoint = aiProperties.getBaseUrl().replaceAll("/+$", "") + "/chat/completions";

            ResponseEntity<String> response = restTemplate.postForEntity(endpoint, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode choices = root.path("choices");
                if (choices.isArray() && !choices.isEmpty()) {
                    return choices.get(0).path("message").path("content").asText();
                }
            }
            throw new AiServiceException("Failed to obtain valid response content from AI provider.");

        } catch (Exception e) {
            log.error("AI provider interaction error: {}", e.getMessage());
            throw new AiServiceException("AI service request failed. Please try again later.");
        }
    }
}