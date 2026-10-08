package com.samrat.ai.developer.assessment.adaptive.service;

import com.samrat.ai.developer.assessment.submission.model.Submission;
import com.samrat.ai.developer.assessment.submission.model.SubmissionStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecentPerformanceCalculator {

    public double calculateWeightedSuccessRate(List<Submission> recentSubmissions) {
        if (recentSubmissions == null || recentSubmissions.isEmpty()) {
            return 0.0;
        }

        double totalWeight = 0.0;
        double weightedScoreSum = 0.0;
        double decayFactor = 0.90; // Weight decays by 10% per step back

        for (int i = 0; i < recentSubmissions.size(); i++) {
            Submission sub = recentSubmissions.get(i);
            if (sub.getStatus() == SubmissionStatus.SYSTEM_ERROR) {
                continue; // Ignore system error from skill calculation
            }

            double weight = Math.pow(decayFactor, i);
            double scoreNormalized = (sub.getTotalPoints() != null && sub.getTotalPoints() > 0)
                    ? (double) sub.getScore() / 100.0
                    : (sub.getStatus() == SubmissionStatus.ACCEPTED ? 1.0 : 0.0);

            weightedScoreSum += scoreNormalized * weight;
            totalWeight += weight;
        }

        return totalWeight > 0 ? (weightedScoreSum / totalWeight) : 0.0;
    }

    public double calculateConsistencyScore(List<Submission> recentSubmissions) {
        if (recentSubmissions == null || recentSubmissions.size() < 2) {
            return 1.0; // Assume baseline consistency if insufficient data
        }

        double mean = recentSubmissions.stream()
                .filter(s -> s.getStatus() != SubmissionStatus.SYSTEM_ERROR)
                .mapToDouble(s -> s.getScore() / 100.0)
                .average()
                .orElse(0.0);

        double varianceSum = 0.0;
        int count = 0;

        for (Submission s : recentSubmissions) {
            if (s.getStatus() == SubmissionStatus.SYSTEM_ERROR) continue;
            double val = s.getScore() / 100.0;
            varianceSum += Math.pow(val - mean, 2);
            count++;
        }

        if (count < 2) return 1.0;

        double variance = varianceSum / count;
        double stdDev = Math.sqrt(variance);

        // Lower standard deviation = higher consistency (0.0 to 1.0)
        return Math.max(0.0, 1.0 - (stdDev * 2.0));
    }
}
