package com.samrat.ai.developer.assessment.execution.model;

import lombok.Getter;

@Getter
public enum LanguageExecutionConfig {
    JAVA(
            SupportedLanguage.JAVA,
            "eclipse-temurin:21-jdk-alpine", // <-- FIXED THIS LINE
            "Main.java",
            "javac Main.java",
            "java Main"
    ),
    PYTHON(
            SupportedLanguage.PYTHON,
            "python:3.11-slim",
            "main.py",
            null,
            "python3 main.py"
    ),
    JAVASCRIPT(
            SupportedLanguage.JAVASCRIPT,
            "node:20-slim",
            "main.js",
            null,
            "node main.js"
    ),
    CPP(
            SupportedLanguage.CPP,
            "gcc:13",
            "main.cpp",
            "g++ -O2 main.cpp -o main",
            "./main"
    );

    private final SupportedLanguage language;
    private final String dockerImage;
    private final String sourceFileName;
    private final String compileCommand;
    private final String runCommand;

    LanguageExecutionConfig(SupportedLanguage language, String dockerImage, String sourceFileName, String compileCommand, String runCommand) {
        this.language = language;
        this.dockerImage = dockerImage;
        this.sourceFileName = sourceFileName;
        this.compileCommand = compileCommand;
        this.runCommand = runCommand;
    }

    public static LanguageExecutionConfig fromLanguage(SupportedLanguage language) {
        for (LanguageExecutionConfig config : values()) {
            if (config.getLanguage() == language) {
                return config;
            }
        }
        throw new IllegalArgumentException("Unsupported language: " + language);
    }
}