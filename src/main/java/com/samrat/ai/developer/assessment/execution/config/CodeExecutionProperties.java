package com.samrat.ai.developer.assessment.execution.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "code.execution")
public class CodeExecutionProperties {
    private boolean enabled = true;
    private long timeoutMs = 15000;
    private String memoryLimit = "256m";
    private String cpuLimit = "1.0";
    private long maxOutputBytes = 1048576; // 1 MB
    private long maxSourceCodeBytes = 102400; // 100 KB
    private int maxProcesses = 32;
    private int maxExecutionsPerMinute = 10;
}
