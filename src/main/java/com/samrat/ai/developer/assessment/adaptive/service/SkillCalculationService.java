package com.samrat.ai.developer.assessment.adaptive.service;

import com.samrat.ai.developer.assessment.adaptive.config.AdaptiveProperties;
import com.samrat.ai.developer.assessment.adaptive.model.SkillLevel;
import com.samrat.ai.developer.assessment.adaptive.model.SkillPerformance;
import com.samrat.ai.developer.assessment.adaptive.model.UserSkillProfile;
import com.samrat.ai.developer.assessment.adaptive.repository.SkillPerformanceRepository;
import com.samrat.ai.developer.assessment.adaptive.repository.UserSkillProfileRepository;
import com.samrat.ai.developer.assessment.problem.model.Difficulty;
import com.samrat.ai.developer.assessment.problem.model.Topic;
import com.samrat.ai.developer.assessment.submission.model.Submission;
import com.samrat.ai.developer.assessment.submission.model.SubmissionStatus;
import com.samrat.ai.developer.assessment.submission.repository.SubmissionRepository;
import com.samrat.ai.developer.assessment.user.model.User;
import com.samrat.ai.developer.assessment.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SkillCalculationService {

    private final UserSkillProfileRepository profileRepository;
    private final SkillPerformanceRepository performanceRepository;
    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final RecentPerformanceCalculator recentCalculator;
    private final AdaptiveProperties adaptiveProperties;

    @Transactional
    public UserSkillProfile calculateUserSkillProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));

        UserSkillProfile profile = profileRepository.findByUserId(userId)
                .orElseGet(() -> profileRepository.save(UserSkillProfile.builder().user(user).build()));

        List<Submission> allValidSubmissions = submissionRepository
                .findByUserIdOrderBySubmittedAtDesc(userId, PageRequest.of(0, 500))
                .getContent();

        if (allValidSubmissions.isEmpty()) {
            profile.setOverallScore(null);
            profile.setOverallLevel(null);
            profile.setLastCalculatedAt(LocalDateTime.now());
            return profileRepository.save(profile);
        }

        Map<Topic, List<Submission>> topicSubmissionsMap = allValidSubmissions.stream()
                .filter(s -> s.getProblem() != null && s.getProblem().getTopic() != null)
                .collect(Collectors.groupingBy(s -> s.getProblem().getTopic()));

        double totalWeightedTopicScore = 0.0;
        int activeTopicsCount = 0;

        for (Topic topic : Topic.values()) {
            List<Submission> topicSubs = topicSubmissionsMap.getOrDefault(topic, Collections.emptyList());
            if (!topicSubs.isEmpty()) {
                SkillPerformance sp = updateTopicSkill(profile, topic, topicSubs);
                totalWeightedTopicScore += sp.getSkillScore();
                activeTopicsCount++;
            }
        }

        if (activeTopicsCount > 0) {
            double overall = totalWeightedTopicScore / activeTopicsCount;
            profile.setOverallScore(Math.round(overall * 10.0) / 10.0);
            profile.setOverallLevel(SkillLevel.fromScore(overall));
        }

        profile.setLastCalculatedAt(LocalDateTime.now());
        return profileRepository.save(profile);
    }

    @Transactional
    public UserSkillProfile updateProfileForUser(Long userId) {
        return calculateUserSkillProfile(userId);
    }

    private SkillPerformance updateTopicSkill(UserSkillProfile profile, Topic topic, List<Submission> topicSubs) {
        SkillPerformance sp = performanceRepository.findByUserSkillProfileIdAndTopic(profile.getId(), topic)
                .orElseGet(() -> SkillPerformance.builder()
                        .userSkillProfile(profile)
                        .topic(topic)
                        .currentDifficulty(Difficulty.EASY)
                        .build());

        int total = topicSubs.size();
        int accepted = (int) topicSubs.stream()
                .filter(s -> s.getStatus() == SubmissionStatus.ACCEPTED)
                .count();

        double successRate = total > 0 ? (double) accepted / total : 0.0;
        double avgScore = topicSubs.stream()
                .mapToDouble(s -> s.getScore() != null ? s.getScore() : 0.0)
                .average()
                .orElse(0.0);

        double avgExecTime = topicSubs.stream()
                .mapToLong(s -> s.getExecutionTimeMs() != null ? s.getExecutionTimeMs() : 0L)
                .average()
                .orElse(0.0);

        // Filter by difficulties safely
        List<Submission> easySubs = filterByDiff(topicSubs, Difficulty.EASY);
        List<Submission> medSubs = filterByDiff(topicSubs, Difficulty.MEDIUM);
        List<Submission> hardSubs = filterByDiff(topicSubs, Difficulty.HARD);

        sp.setTotalAttempts(total);
        sp.setAcceptedAttempts(accepted);
        sp.setSuccessRate(successRate);
        sp.setAverageScore(avgScore);
        sp.setAverageExecutionTimeMs(avgExecTime);

        sp.setEasyAttempts(easySubs.size());
        sp.setEasySuccessRate(calcRate(easySubs));
        sp.setMediumAttempts(medSubs.size());
        sp.setMediumSuccessRate(calcRate(medSubs));
        sp.setHardAttempts(hardSubs.size());
        sp.setHardSuccessRate(calcRate(hardSubs));

        int windowSize = adaptiveProperties != null && adaptiveProperties.getRecentAttemptWindow() > 0
                ? adaptiveProperties.getRecentAttemptWindow()
                : 10;

        List<Submission> recentWindow = topicSubs.stream()
                .limit(windowSize)
                .toList();

        double recentSuccessRate = recentCalculator.calculateWeightedSuccessRate(recentWindow);
        double consistency = recentCalculator.calculateConsistencyScore(recentWindow);

        sp.setRecentSuccessRate(recentSuccessRate);
        sp.setConsistencyScore(consistency);

        // Difficulty Weight Factor Calculation
        double difficultyFactor = 0.33;
        if (!hardSubs.isEmpty() && sp.getHardSuccessRate() >= 0.5) {
            difficultyFactor = 1.0;
        } else if (!medSubs.isEmpty() && sp.getMediumSuccessRate() >= 0.5) {
            difficultyFactor = 0.66;
        }

        // Formula: 0.40 * recentSuccessRate + 0.25 * (avgScore / 100) + 0.20 * difficultyFactor + 0.15 * consistency
        double skillScoreNormalized = (0.40 * recentSuccessRate)
                + (0.25 * (avgScore / 100.0))
                + (0.20 * difficultyFactor)
                + (0.15 * consistency);

        double calculatedScore = Math.min(100.0, Math.max(0.0, skillScoreNormalized * 100.0));
        sp.setSkillScore((int) (Math.round(calculatedScore * 10.0) / 10.0));
        sp.setSkillLevel(SkillLevel.fromScore(Double.valueOf(sp.getSkillScore())));

        if (!topicSubs.isEmpty() && topicSubs.get(0).getSubmittedAt() != null) {
            sp.setLastAttemptAt(topicSubs.get(0).getSubmittedAt());
        }

        return performanceRepository.save(sp);
    }

    private List<Submission> filterByDiff(List<Submission> subs, Difficulty diff) {
        return subs.stream()
                .filter(s -> s.getProblem() != null && s.getProblem().getDifficulty() == diff)
                .toList();
    }

    private double calcRate(List<Submission> subs) {
        if (subs.isEmpty()) return 0.0;
        long passed = subs.stream()
                .filter(s -> Objects.equals(s.getStatus(), SubmissionStatus.ACCEPTED))
                .count();
        return (double) passed / subs.size();
    }
}
