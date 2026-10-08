package com.samrat.ai.developer.assessment.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "ai")
public class AiProperties {
    private boolean enabled = true;
    private String provider = "openai";
    private String apiKey = "";
    private String model = "gpt-4o-mini";
    private String baseUrl = "https://api.openai.com/v1";
    private int timeoutSeconds = 30;
    private int maxInputChars = 30000;
    private int maxOutputTokens = 2000;
    private int reviewRateLimit = 5;
    private int hintRateLimit = 10;
    private int rateLimitWindowMinutes = 60;
}