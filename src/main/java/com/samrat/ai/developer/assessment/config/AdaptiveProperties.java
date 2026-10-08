package com.samrat.ai.developer.assessment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Configuration
@ConfigurationProperties(prefix = "adaptive")
@Component("adaptiveConfigProperties")
public class AdaptiveProperties {
    private double successThreshold = 0.80;
    private double maintainThreshold = 0.60;
    private double recoveryThreshold = 0.40;
    private int minAttemptsForPromotion = 3;
    private int recentAttemptWindow = 10;
    private int diagnosticQuestionCount = 6;
    private double exploitationRatio = 0.70;
    private double explorationRatio = 0.30;
    private int minProblemRepeatDays = 30;
}