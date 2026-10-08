package com.samrat.ai.developer.assessment.analytics.dto;

import com.samrat.ai.developer.assessment.problem.model.Difficulty;
import com.samrat.ai.developer.assessment.problem.model.Topic;
import com.samrat.ai.developer.assessment.submission.model.SubmissionStatus;

import java.time.LocalDateTime;

public record RecentActivityResponse(
        Long submissionId,
        Long problemId,
        String problemTitle,
        Topic topic,
        Difficulty difficulty,
        SubmissionStatus status,
        Integer score,
        LocalDateTime submittedAt
) {}
