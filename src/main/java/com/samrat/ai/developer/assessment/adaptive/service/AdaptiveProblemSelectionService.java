package com.samrat.ai.developer.assessment.adaptive.service;

import com.samrat.ai.developer.assessment.adaptive.config.AdaptiveProperties;
import com.samrat.ai.developer.assessment.problem.model.Difficulty;
import com.samrat.ai.developer.assessment.problem.model.Problem;
import com.samrat.ai.developer.assessment.problem.model.Topic;
import com.samrat.ai.developer.assessment.problem.repository.ProblemRepository;
import com.samrat.ai.developer.assessment.submission.model.Submission;
import com.samrat.ai.developer.assessment.submission.model.SubmissionStatus;
import com.samrat.ai.developer.assessment.submission.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdaptiveProblemSelectionService {

    private final ProblemRepository problemRepository;
    private final SubmissionRepository submissionRepository = null;
    private final AdaptiveProperties adaptiveProperties;

    public Optional<Problem> selectProblem(Long userId, Topic topic, Difficulty difficulty) {
        List<Problem> candidates = problemRepository.findByTopicAndDifficulty(topic, difficulty);
        if (candidates.isEmpty()) {
            // Fallback to any difficulty in topic if specified difficulty has no problems
            candidates = problemRepository.findByTopic(topic);
        }

        if (candidates.isEmpty()) {
            return Optional.empty();
        }

        List<Submission> userSubs = submissionRepository.findByUserIdOrderBySubmittedAtDesc(userId, PageRequest.of(0, 200)).getContent();

        Set<Long> solvedProblemIds = userSubs.stream()
                .filter(s -> s.getStatus() == SubmissionStatus.ACCEPTED)
                .map(s -> s.getProblem().getId())
                .collect(Collectors.toSet());

        Set<Long> recentlyAttemptedProblemIds = userSubs.stream()
                .filter(s -> s.getSubmittedAt().isAfter(LocalDateTime.now().minusDays(adaptiveProperties.getMinProblemRepeatDays())))
                .map(s -> s.getProblem().getId())
                .collect(Collectors.toSet());

        // 1. Unsolved & Not Recently Attempted
        List<Problem> freshCandidates = candidates.stream()
                .filter(p -> !solvedProblemIds.contains(p.getId()) && !recentlyAttemptedProblemIds.contains(p.getId()))
                .collect(Collectors.toList());

        if (!freshCandidates.isEmpty()) {
            return Optional.of(freshCandidates.get(0));
        }

        // 2. Unsolved
        List<Problem> unsolvedCandidates = candidates.stream()
                .filter(p -> !solvedProblemIds.contains(p.getId()))
                .collect(Collectors.toList());

        if (!unsolvedCandidates.isEmpty()) {
            return Optional.of(unsolvedCandidates.get(0));
        }

        // 3. Fallback to any problem
        return Optional.of(candidates.get(0));
    }
}
