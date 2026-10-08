// AdminDashboardService.java
package com.samrat.ai.developer.assessment.admin.service;

import com.samrat.ai.developer.assessment.admin.dto.AdminDTOs.*;
import com.samrat.ai.developer.assessment.admin.repository.AdminStatisticsRepository;
import com.samrat.ai.developer.assessment.assessment.model.AssessmentSession;
import com.samrat.ai.developer.assessment.assessment.repository.AssessmentSessionRepository;
import com.samrat.ai.developer.assessment.problem.model.Difficulty;
import com.samrat.ai.developer.assessment.problem.model.Topic;
import com.samrat.ai.developer.assessment.submission.model.Submission;
import com.samrat.ai.developer.assessment.submission.model.SubmissionStatus;
import com.samrat.ai.developer.assessment.submission.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final AdminStatisticsRepository statisticsRepository;
    private final SubmissionRepository submissionRepository;
    private final AssessmentSessionRepository assessmentSessionRepository;

    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboardOverview() {
        long totalUsers = statisticsRepository.countTotalUsers();
        long totalProblems = statisticsRepository.countTotalProblems();
        long totalSubmissions = statisticsRepository.countTotalSubmissions();
        long totalAssessments = statisticsRepository.countTotalAssessments();
        long activeAssessments = statisticsRepository.countActiveAssessments();
        long acceptedSubmissions = statisticsRepository.countAcceptedSubmissions();
        var avgScore = statisticsRepository.calculateAverageSubmissionScore();
        long pendingSimilarity = statisticsRepository.countPendingSimilarityReviews();
        var completionRate = statisticsRepository.calculateAssessmentCompletionRate();

        // Topic & Difficulty Maps
        Map<Difficulty, Long> difficultyMap = new EnumMap<>(Difficulty.class);
        statisticsRepository.getProblemsByDifficulty().forEach((k, v) -> {
            try { difficultyMap.put(Difficulty.valueOf(k), v); } catch (Exception ignored) {}
        });

        Map<Topic, Long> topicMap = new EnumMap<>(Topic.class);
        statisticsRepository.getProblemsByTopic().forEach((k, v) -> {
            try { topicMap.put(Topic.valueOf(k), v); } catch (Exception ignored) {}
        });

        Map<SubmissionStatus, Long> statusMap = new EnumMap<>(SubmissionStatus.class);
        statisticsRepository.getSubmissionsByStatus().forEach((k, v) -> {
            try { statusMap.put(SubmissionStatus.valueOf(k), v); } catch (Exception ignored) {}
        });

        // Combined activity feed: latest submissions and session outcomes
        List<RecentActivityItem> activity = new ArrayList<>();

        List<Submission> recentSubs = submissionRepository.findAll(PageRequest.of(0, 5)).getContent();
        for (Submission s : recentSubs) {
            activity.add(new RecentActivityItem(
                    "SUBMISSION",
                    s.getUser().getName() + " submitted " + s.getProblem().getTitle(),
                    "Language: " + s.getLanguage() + " | Score: " + s.getScore(),
                    s.getStatus().name(),
                    s.getSubmittedAt()
            ));
        }

        activity.sort(Comparator.comparing(RecentActivityItem::timestamp).reversed());

        return new AdminDashboardResponse(
                totalUsers,
                totalProblems,
                totalSubmissions,
                totalAssessments,
                activeAssessments,
                acceptedSubmissions,
                avgScore,
                pendingSimilarity,
                completionRate,
                difficultyMap,
                topicMap,
                statusMap,
                activity
        );
    }
}