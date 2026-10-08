// AdminDTOs.java
package com.samrat.ai.developer.assessment.admin.dto;

import com.samrat.ai.developer.assessment.problem.model.Difficulty;
import com.samrat.ai.developer.assessment.problem.model.Topic;
import com.samrat.ai.developer.assessment.submission.model.SubmissionStatus;
import com.samrat.ai.developer.assessment.user.model.Role;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class AdminDTOs {

    public record AdminDashboardResponse(
            long totalUsers,
            long totalProblems,
            long totalSubmissions,
            long totalAssessments,
            long activeAssessments,
            long acceptedSubmissions,
            BigDecimal averageSubmissionScore,
            long pendingSimilarityReviews,
            BigDecimal assessmentCompletionRate,
            Map<Difficulty, Long> problemsByDifficulty,
            Map<Topic, Long> problemsByTopic,
            Map<SubmissionStatus, Long> submissionsByStatus,
            List<RecentActivityItem> recentActivity
    ) {}

    public record RecentActivityItem(
            String type, // SUBMISSION, ASSESSMENT, SIMILARITY, USER_REGISTRATION
            String title,
            String description,
            String status,
            LocalDateTime timestamp
    ) {}

    public record UserAdminSummary(
            Long id,
            String name,
            String email,
            Role role,
            LocalDateTime createdAt,
            long submissionCount,
            long assessmentCount
    ) {}

    public record UserAdminDetailResponse(
            Long id,
            String name,
            String email,
            Role role,
            LocalDateTime createdAt,
            long totalSubmissions,
            long acceptedSubmissions,
            BigDecimal averageScore,
            long assessmentsCompleted,
            List<UserTopicSkillOverview> skills
    ) {
        public record UserTopicSkillOverview(
                Topic topic,
                BigDecimal score,
                String level
        ) {}
    }

    public record AdminSubmissionOverview(
            Long id,
            Long userId,
            String userName,
            Long problemId,
            String problemTitle,
            String language,
            SubmissionStatus status,
            BigDecimal score,
            int passedTests,
            int totalTests,
            Long executionTimeMs,
            LocalDateTime submittedAt
    ) {}

    public record AdminSubmissionDetail(
            Long id,
            Long userId,
            String userName,
            String userEmail,
            Long problemId,
            String problemTitle,
            String language,
            String sourceCode,
            SubmissionStatus status,
            BigDecimal score,
            int passedTests,
            int totalTests,
            Long executionTimeMs,
            LocalDateTime submittedAt
    ) {}

    public record SimilarityStatusUpdateRequest(
            String status // REVIEWED, CLEARED, REQUIRES_ATTENTION, UNDER_REVIEW
    ) {}
}
