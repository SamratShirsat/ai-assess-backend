package com.samrat.ai.developer.assessment.ai.client;

public interface AiClient {
    String generate(String systemPrompt, String userPrompt);
}