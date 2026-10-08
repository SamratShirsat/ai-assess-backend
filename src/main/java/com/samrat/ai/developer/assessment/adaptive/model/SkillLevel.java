package com.samrat.ai.developer.assessment.adaptive.model;

public enum SkillLevel {
    NOVICE,
    BEGINNER,
    INTERMEDIATE,
    ADVANCED,
    EXPERT;

    public static SkillLevel fromScore(Double score) {
        if (score == null) return NOVICE;
        if (score < 20.0) return NOVICE;
        if (score < 40.0) return BEGINNER;
        if (score < 60.0) return INTERMEDIATE;
        if (score < 80.0) return ADVANCED;
        return EXPERT;
    }
}
