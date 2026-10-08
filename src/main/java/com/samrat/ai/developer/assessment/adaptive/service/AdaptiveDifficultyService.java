package com.samrat.ai.developer.assessment.adaptive.service;

import com.samrat.ai.developer.assessment.adaptive.config.AdaptiveProperties;
import com.samrat.ai.developer.assessment.adaptive.model.SkillPerformance;
import com.samrat.ai.developer.assessment.problem.model.Difficulty;
import com.samrat.ai.developer.assessment.submission.model.Submission;
import com.samrat.ai.developer.assessment.submission.model.SubmissionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdaptiveDifficultyService {

    private final AdaptiveProperties adaptiveProperties;

    public Difficulty determineNextDifficulty(SkillPerformance performance, List<Submission> recentTopicSubmissions) {
        if (performance == null || recentTopicSubmissions == null || recentTopicSubmissions.size() < adaptiveProperties.getMinAttemptsForPromotion()) {
            return performance != null ? performance.getCurrentDifficulty() : Difficulty.EASY;
        }

        Difficulty current = performance.getCurrentDifficulty();
        List<Submission> window = recentTopicSubmissions.subList(0, Math.min(recentTopicSubmissions.size(), adaptiveProperties.getRecentAttemptWindow()));

        long total = window.size();
        long passed = window.stream().filter(s -> s.getStatus() == SubmissionStatus.ACCEPTED).count();
        double recentRate = (double) passed / total;

        if (recentRate >= adaptiveProperties.getSuccessThreshold() && total >= adaptiveProperties.getMinAttemptsForPromotion()) {
            if (current == Difficulty.EASY) return Difficulty.MEDIUM;
            if (current == Difficulty.MEDIUM) return Difficulty.HARD;
        } else if (recentRate < adaptiveProperties.getRecoveryThreshold() && total >= adaptiveProperties.getMinAttemptsForPromotion()) {
            if (current == Difficulty.HARD) return Difficulty.MEDIUM;
            if (current == Difficulty.MEDIUM) return Difficulty.EASY;
        }

        return current;
    }
}
