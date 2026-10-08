package com.samrat.ai.developer.assessment.adaptive.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "adaptive")
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
