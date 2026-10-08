package com.samrat.ai.developer.assessment.analytics.service;

import com.samrat.ai.developer.assessment.adaptive.model.SkillPerformance;
import com.samrat.ai.developer.assessment.adaptive.model.UserSkillProfile;
import com.samrat.ai.developer.assessment.adaptive.model.SkillLevel;
import com.samrat.ai.developer.assessment.adaptive.repository.SkillPerformanceRepository;
import com.samrat.ai.developer.assessment.adaptive.repository.UserSkillProfileRepository;
import com.samrat.ai.developer.assessment.analytics.dto.*;
import com.samrat.ai.developer.assessment.analytics.model.PerformanceTrend;
import com.samrat.ai.developer.assessment.analytics.repository.AnalyticsRepository;
import com.samrat.ai.developer.assessment.problem.model.Difficulty;
import com.samrat.ai.developer.assessment.problem.model.Topic;
import com.samrat.ai.developer.assessment.submission.model.Submission;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private final AnalyticsRepository analyticsRepository;
    private final UserSkillProfileRepository userSkillProfileRepository;
    private final SkillPerformanceRepository skillPerformanceRepository;

    @Value("${analytics.trend.recent-submissions:5}")
    private int recentSubmissionsWindow = 5;

    @Value("${analytics.trend.threshold:10}")
    private double trendThreshold = 10.0;

    public AnalyticsService(AnalyticsRepository analyticsRepository,
                            UserSkillProfileRepository userSkillProfileRepository,
                            SkillPerformanceRepository skillPerformanceRepository) {
        this.analyticsRepository = analyticsRepository;
        this.userSkillProfileRepository = userSkillProfileRepository;
        this.skillPerformanceRepository = skillPerformanceRepository;
    }

    public DashboardResponse getDashboard(Long userId) {
        OverallAnalyticsResponse overall = getOverallAnalytics(userId);
        List<TopicAnalyticsResponse> topics = getTopicAnalytics(userId);
        List<DifficultyAnalyticsResponse> difficulties = getDifficultyAnalytics(userId);
        List<RecentActivityResponse> recent = getRecentActivity(userId, 10);

        List<TopicScoreSummary> strongest = getStrongestTopics(userId, 3);
        List<TopicScoreSummary> weakest = getWeakestTopics(userId, 3);

        return new DashboardResponse(overall, strongest, weakest, topics, difficulties, recent);
    }

    public OverallAnalyticsResponse getOverallAnalytics(Long userId) {
        long totalSubmissions = analyticsRepository.countTotalSubmissions(userId);
        if (totalSubmissions == 0) {
            return new OverallAnalyticsResponse(
                    0, SkillLevel.NOVICE, 0L, 0L, 0L, 0L,
                    BigDecimal.ZERO, BigDecimal.ZERO, null, null, PerformanceTrend.STABLE
            );
        }

        UserSkillProfile profile = userSkillProfileRepository.findByUserId(userId).orElse(null);
        double skillScore = (profile != null) ? profile.getOverallScore() : 0;
        SkillLevel skillLevel = (profile != null) ? profile.getOverallLevel() : SkillLevel.NOVICE;

        long acceptedSubmissions = analyticsRepository.countAcceptedSubmissions(userId);
        long totalProblems = analyticsRepository.countTotalProblemsAttempted(userId);
        long solvedProblems = analyticsRepository.countTotalProblemsSolved(userId);
        Double avgScoreVal = analyticsRepository.getAverageScoreForUser(userId);

        BigDecimal acceptanceRate = calculatePercentage(acceptedSubmissions, totalSubmissions);
        BigDecimal avgScore = (avgScoreVal != null) ? BigDecimal.valueOf(avgScoreVal).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        Topic strongestTopic = getStrongestTopic(userId);
        Topic weakestTopic = getWeakestTopic(userId);
        PerformanceTrend overallTrend = calculateUserTrend(userId, null);

        return new OverallAnalyticsResponse(
                skillScore, skillLevel, totalProblems, solvedProblems,
                totalSubmissions, acceptedSubmissions, acceptanceRate, avgScore,
                strongestTopic, weakestTopic, overallTrend
        );
    }

    public List<TopicAnalyticsResponse> getTopicAnalytics(Long userId) {
        Map<Topic, SkillPerformance> perfMap = skillPerformanceRepository.findByUserId(userId)
                .stream().collect(Collectors.toMap(SkillPerformance::getTopic, p -> p));

        List<TopicAnalyticsResponse> list = new ArrayList<>();
        for (Topic topic : Topic.values()) {
            SkillPerformance sp = perfMap.get(topic);
            double score = (sp != null) ? sp.getSkillScore() : 0;
            SkillLevel level = (sp != null) ? sp.getSkillLevel() : SkillLevel.NOVICE;

            long attempted = analyticsRepository.countSubmissionsByTopic(userId, topic);
            long solved = analyticsRepository.countSolvedProblemsByTopic(userId, topic);
            Double avgScoreVal = analyticsRepository.getAverageScoreByTopic(userId, topic);

            BigDecimal successRate = calculatePercentage(solved, attempted);
            BigDecimal avgScore = (avgScoreVal != null) ? BigDecimal.valueOf(avgScoreVal).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            PerformanceTrend trend = calculateUserTrend(userId, topic);

            list.add(new TopicAnalyticsResponse(topic, score, level, attempted, solved, successRate, avgScore, trend));
        }
        return list;
    }

    public TopicAnalyticsResponse getTopicAnalyticsByTopic(Long userId, Topic topic) {
        return getTopicAnalytics(userId).stream()
                .filter(t -> t.topic() == topic)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Topic not found: " + topic));
    }

    public List<DifficultyAnalyticsResponse> getDifficultyAnalytics(Long userId) {
        List<DifficultyAnalyticsResponse> result = new ArrayList<>();
        for (Difficulty diff : Difficulty.values()) {
            long attempts = analyticsRepository.countSubmissionsByDifficulty(userId, diff);
            long solved = analyticsRepository.countSolvedProblemsByDifficulty(userId, diff);
            Double avgVal = analyticsRepository.getAverageScoreByDifficulty(userId, diff);

            BigDecimal successRate = calculatePercentage(solved, attempts);
            BigDecimal avgScore = (avgVal != null) ? BigDecimal.valueOf(avgVal).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

            result.add(new DifficultyAnalyticsResponse(diff, attempts, solved, successRate, avgScore));
        }
        return result;
    }

    public List<RecentActivityResponse> getRecentActivity(Long userId, int limit) {
        return analyticsRepository.findRecentActivities(userId, PageRequest.of(0, limit));
    }

    public PerformanceTrend getOverallTrend(Long userId) {
        return calculateUserTrend(userId, null);
    }

    public List<TopicScoreSummary> getStrongestTopics(Long userId, int limit) {
        return skillPerformanceRepository.findByUserId(userId).stream()
                .filter(p -> p.getTotalAttempts() >= 2)
                .sorted(Comparator.comparingInt(SkillPerformance::getSkillScore).reversed())
                .limit(limit)
                .map(p -> new TopicScoreSummary(p.getTopic(), p.getSkillScore()))
                .collect(Collectors.toList());
    }

    public List<TopicScoreSummary> getWeakestTopics(Long userId, int limit) {
        return skillPerformanceRepository.findByUserId(userId).stream()
                .filter(p -> p.getTotalAttempts() >= 2)
                .sorted(Comparator.comparingInt(SkillPerformance::getSkillScore))
                .limit(limit)
                .map(p -> new TopicScoreSummary(p.getTopic(), p.getSkillScore()))
                .collect(Collectors.toList());
    }

    private Topic getStrongestTopic(Long userId) {
        List<TopicScoreSummary> list = getStrongestTopics(userId, 1);
        return list.isEmpty() ? null : list.get(0).topic();
    }

    private Topic getWeakestTopic(Long userId) {
        List<TopicScoreSummary> list = getWeakestTopics(userId, 1);
        return list.isEmpty() ? null : list.get(0).topic();
    }

    public PerformanceTrend calculateUserTrend(Long userId, Topic topic) {
        int window = recentSubmissionsWindow;
        List<Submission> submissions = (topic == null)
                ? analyticsRepository.findRecentSubmissions(userId, PageRequest.of(0, window * 2))
                : analyticsRepository.findRecentSubmissionsByTopic(userId, topic, PageRequest.of(0, window * 2));

        if (submissions.size() < window * 2) {
            return PerformanceTrend.STABLE;
        }

        List<Submission> recentList = submissions.subList(0, window);
        List<Submission> previousList = submissions.subList(window, window * 2);

        double recentAvg = recentList.stream().mapToInt(Submission::getScore).average().orElse(0.0);
        double previousAvg = previousList.stream().mapToInt(Submission::getScore).average().orElse(0.0);

        double diff = recentAvg - previousAvg;
        if (diff >= trendThreshold) {
            return PerformanceTrend.IMPROVING;
        } else if (diff <= -trendThreshold) {
            return PerformanceTrend.DECLINING;
        } else {
            return PerformanceTrend.STABLE;
        }
    }

    private BigDecimal calculatePercentage(long numerator, long denominator) {
        if (denominator == 0) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        return BigDecimal.valueOf((double) numerator * 100.0 / denominator).setScale(2, RoundingMode.HALF_UP);
    }
}

