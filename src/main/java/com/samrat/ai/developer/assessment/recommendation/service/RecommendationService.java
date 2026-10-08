package com.samrat.ai.developer.assessment.recommendation.service;

import com.samrat.ai.developer.assessment.problem.model.Problem;
import com.samrat.ai.developer.assessment.user.model.User;
import com.samrat.ai.developer.assessment.problem.repository.ProblemRepository;
import com.samrat.ai.developer.assessment.submission.repository.SubmissionRepository;
import com.samrat.ai.developer.assessment.user.repository.UserRepository;
import com.samrat.ai.developer.assessment.recommendation.config.RecommendationProperties;
import com.samrat.ai.developer.assessment.recommendation.dto.RecommendationFeedbackRequest;
import com.samrat.ai.developer.assessment.recommendation.dto.RecommendationFeedbackResponse;
import com.samrat.ai.developer.assessment.recommendation.dto.RecommendationResponse;
import com.samrat.ai.developer.assessment.adaptive.model.AdaptiveRecommendation;
import com.samrat.ai.developer.assessment.recommendation.repository.RecommendationRepository;
import com.samrat.ai.developer.assessment.adaptive.service.UserSkillProfileService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final ProblemRepository problemRepository;
    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final UserSkillProfileService userSkillProfileService;
    private final RecommendationScoringService scoringService;
    private final RecommendationReasonService reasonService;
    private final RecommendationRepository recommendationRepository;
    private final RecommendationProperties properties;

    public RecommendationService(
            ProblemRepository problemRepository,
            SubmissionRepository submissionRepository,
            UserRepository userRepository,
            UserSkillProfileService userSkillProfileService,
            RecommendationScoringService scoringService,
            RecommendationReasonService reasonService,
            RecommendationRepository recommendationRepository,
            RecommendationProperties properties
    ) {
        this.problemRepository = problemRepository;
        this.submissionRepository = submissionRepository;
        this.userRepository = userRepository;
        this.userSkillProfileService = userSkillProfileService;
        this.scoringService = scoringService;
        this.reasonService = reasonService;
        this.recommendationRepository = recommendationRepository;
        this.properties = properties;
    }

    @Transactional
    public List<RecommendationResponse> getPersonalizedRecommendations(Long userId, int limit, String topicFilter, String difficultyFilter) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        int boundedLimit = Math.min(Math.max(limit, 1), 20);

        double overallScore = userSkillProfileService.getOverallSkillScore(userId);
        Map<String, Double> topicScores = userSkillProfileService.getTopicSkillScores(userId);
        Map<String, Integer> topicAttempts = userSkillProfileService.getTopicAttemptCounts(userId);

        Set<Long> solvedProblemIds = new HashSet<>(submissionRepository.findSolvedProblemIdsByUserId(userId, com.samrat.ai.developer.assessment.submission.model.SubmissionStatus.ACCEPTED));
        Set<Long> attemptedProblemIds = new HashSet<>(submissionRepository.findAllAttemptedProblemIdsByUserId(userId));
        List<Long> recentProblemIds = submissionRepository.findRecentAttemptedProblemIdsByUserId(
                userId, PageRequest.of(0, properties.getRecentAttemptWindow())
        );

        List<Problem> candidateProblems = problemRepository.findAll();

        List<Problem> filteredCandidates = candidateProblems.stream()
                .filter(p -> topicFilter == null || topicFilter.isEmpty() || p.getTopic().toString().equalsIgnoreCase(topicFilter))
                .filter(p -> difficultyFilter == null || difficultyFilter.isEmpty() || p.getDifficulty().toString().equalsIgnoreCase(difficultyFilter))
                .collect(Collectors.toList());

        List<Problem> primaryCandidates = filteredCandidates.stream()
                .filter(p -> !solvedProblemIds.contains(p.getId()) && !recentProblemIds.contains(p.getId()))
                .collect(Collectors.toList());

        List<Problem> selectedPool = primaryCandidates;
        if (selectedPool.isEmpty()) {
            selectedPool = filteredCandidates.stream()
                    .filter(p -> !solvedProblemIds.contains(p.getId()))
                    .collect(Collectors.toList());
        }
        if (selectedPool.isEmpty()) {
            selectedPool = filteredCandidates;
        }

        List<RecommendationResponse> rankedRecommendations = new ArrayList<>();
        for (Problem problem : selectedPool) {
            boolean isAttempted = attemptedProblemIds.contains(problem.getId());
            boolean isSolved = solvedProblemIds.contains(problem.getId());
            boolean isRecent = recentProblemIds.contains(problem.getId());

            RecommendationScoringService.EvaluationResult eval = scoringService.evaluateCandidate(
                    problem, overallScore, topicScores, topicAttempts, isAttempted, isSolved, isRecent
            );

            String topicKey = problem.getTopic() != null ? problem.getTopic().toString() : "GENERAL";
            double topicScore = topicScores.getOrDefault(topicKey, overallScore);
            int attempts = topicAttempts.getOrDefault(topicKey, 0);

            String explanation = reasonService.generateExplanation(problem, eval.primaryReason, topicScore, overallScore, attempts);

            RecommendationResponse response = new RecommendationResponse();
            response.setProblemId(problem.getId());
            response.setTitle(problem.getTitle());
            response.setTopic(problem.getTopic() != null ? problem.getTopic().toString() : "GENERAL");
            response.setDifficulty(problem.getDifficulty() != null ? problem.getDifficulty().toString() : "EASY");
            response.setSkillScore((int) Math.round(overallScore));
            response.setRecommendationScore(eval.totalScore);
            response.setReasonType(eval.primaryReason);
            response.setReason(explanation);
            response.setPreviouslyAttempted(isAttempted);
            response.setPreviouslySolved(isSolved);

            rankedRecommendations.add(response);
        }

        rankedRecommendations.sort(Comparator.comparingDouble(RecommendationResponse::getRecommendationScore).reversed());

        List<RecommendationResponse> finalResults = rankedRecommendations.stream()
                .limit(boundedLimit)
                .collect(Collectors.toList());

        for (RecommendationResponse rec : finalResults) {
            Problem p = problemRepository.getReferenceById(rec.getProblemId());

            AdaptiveRecommendation entity = new AdaptiveRecommendation();
            entity.setUser(user);
            entity.setProblem(p);

            // NEW FIELDS REQUIRED BY THE MERGED ENTITY
            entity.setTopic(p.getTopic());
            entity.setDifficulty(p.getDifficulty());

            entity.setRecommendationScore(rec.getRecommendationScore());
            entity.setReasonType(rec.getReasonType());

            // UPDATED: Use setReasonMessage instead of setReason
            entity.setReasonMessage(rec.getReason());

            entity.setPreviouslyAttempted(rec.getPreviouslyAttempted());
            entity.setPreviouslySolved(rec.getPreviouslySolved());
            entity.setCreatedAt(LocalDateTime.now());
            entity.setShownAt(LocalDateTime.now());

            AdaptiveRecommendation saved = recommendationRepository.save(entity);
            rec.setId(saved.getId());
        }

        return finalResults;
    }

    @Transactional(readOnly = true)
    public RecommendationResponse getNextRecommendation(Long userId) {
        List<RecommendationResponse> list = getPersonalizedRecommendations(userId, 1, null, null);
        return list.isEmpty() ? null : list.get(0);
    }

    @Transactional(readOnly = true)
    public Page<RecommendationResponse> getRecommendationHistory(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50));
        Page<AdaptiveRecommendation> history = recommendationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        return history.map(entity -> {
            RecommendationResponse dto = new RecommendationResponse();
            dto.setId(entity.getId());
            dto.setProblemId(entity.getProblem().getId());
            dto.setTitle(entity.getProblem().getTitle());
            dto.setTopic(entity.getProblem().getTopic() != null ? entity.getProblem().getTopic().toString() : "GENERAL");
            dto.setDifficulty(entity.getProblem().getDifficulty() != null ? entity.getProblem().getDifficulty().toString() : "EASY");
            dto.setRecommendationScore(entity.getRecommendationScore());
            dto.setReasonType(entity.getReasonType());

            // UPDATED: Use getReasonMessage instead of getReason
            dto.setReason(entity.getReasonMessage());

            dto.setPreviouslyAttempted(entity.getPreviouslyAttempted());
            dto.setPreviouslySolved(entity.getPreviouslySolved());
            return dto;
        });
    }

    @Transactional
    public RecommendationFeedbackResponse submitFeedback(Long userId, Long recommendationId, RecommendationFeedbackRequest request) {
        AdaptiveRecommendation recommendation = recommendationRepository.findByIdAndUserId(recommendationId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Recommendation not found or access denied."));

        recommendation.setFeedback(request.getFeedback().toString());
        recommendationRepository.save(recommendation);

        return new RecommendationFeedbackResponse(
                recommendationId,
                request.getFeedback(),
                "Thank you! Your feedback will help improve future problem suggestions."
        );
    }
}