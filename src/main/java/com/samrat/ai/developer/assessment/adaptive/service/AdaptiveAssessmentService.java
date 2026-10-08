package com.samrat.ai.developer.assessment.adaptive.service;

import com.samrat.ai.developer.assessment.adaptive.config.AdaptiveProperties;
import com.samrat.ai.developer.assessment.adaptive.dto.AdaptiveDTOs.*;
import com.samrat.ai.developer.assessment.adaptive.model.*;
import com.samrat.ai.developer.assessment.adaptive.repository.*;
import com.samrat.ai.developer.assessment.problem.model.Difficulty;
import com.samrat.ai.developer.assessment.problem.model.Problem;
import com.samrat.ai.developer.assessment.problem.model.Topic;
import com.samrat.ai.developer.assessment.problem.repository.ProblemRepository;
import com.samrat.ai.developer.assessment.submission.model.Submission;
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
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdaptiveAssessmentService {

    private final UserRepository userRepository;
    private final ProblemRepository problemRepository;
    private final SubmissionRepository submissionRepository;
    private final UserSkillProfileRepository profileRepository;
    private final SkillPerformanceRepository performanceRepository;
    private final AdaptiveRecommendationRepository recommendationRepository;
    private final DiagnosticAssessmentRepository diagnosticRepository;
    private final SkillCalculationService skillCalculationService;
    private final AdaptiveDifficultyService difficultyService;
    private final AdaptiveTopicSelectionService topicSelectionService;
    private final AdaptiveProblemSelectionService problemSelectionService;
    private final RecommendationExplanationService explanationService;
    private final AdaptiveProperties adaptiveProperties;

    @Transactional
    public AdaptiveRecommendationResponse getNextProblem(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userEmail));

        // 1. Check if user is in Diagnostic Phase
        Optional<DiagnosticAssessment> diagOpt = diagnosticRepository.findFirstByUserIdAndStatus(user.getId(), DiagnosticAssessmentStatus.IN_PROGRESS);
        if (diagOpt.isPresent()) {
            DiagnosticAssessment diag = diagOpt.get();
            List<DiagnosticAssessmentProblem> diagProblems = diag.getProblems();

            for (DiagnosticAssessmentProblem dap : diagProblems) {
                if (!dap.isCompleted()) {
                    Problem p = dap.getProblem();
                    return buildRecommendationResponse(saveTemporaryRecommendation(user, p, RecommendationReasonType.DIAGNOSTIC, "Diagnostic question"));
                }
            }
        }

        // 2. Load or Compute Skill Profile
        UserSkillProfile profile = skillCalculationService.calculateUserSkillProfile(user.getId());
        List<SkillPerformance> performances = performanceRepository.findByUserSkillProfileId(profile.getId());

        // 3. Determine Topic & Difficulty
        Topic targetTopic = topicSelectionService.selectNextTopic(profile, performances);
        SkillPerformance sp = performanceRepository.findByUserSkillProfileIdAndTopic(profile.getId(), targetTopic).orElse(null);

        List<Submission> recentTopicSubs = submissionRepository.findByUserIdOrderBySubmittedAtDesc(user.getId(), PageRequest.of(0, 50)).getContent()
                .stream().filter(s -> s.getProblem().getTopic() == targetTopic).toList();

        Difficulty targetDifficulty = difficultyService.determineNextDifficulty(sp, recentTopicSubs);

        // 4. Determine Reason Type
        RecommendationReasonType reasonType = determineReasonType(sp, targetDifficulty);

        // 5. Select Specific Problem
        Problem selectedProblem = problemSelectionService.selectProblem(user.getId(), targetTopic, targetDifficulty)
                .orElseGet(() -> problemRepository.findAll().stream().findFirst().orElseThrow());

        String reasonMsg = explanationService.generateExplanation(reasonType, targetTopic, targetDifficulty, sp);

        AdaptiveRecommendation rec = AdaptiveRecommendation.builder()
                .user(user)
                .problem(selectedProblem)
                .topic(targetTopic)
                .difficulty(targetDifficulty)
                .reasonType(reasonType)
                .reasonMessage(reasonMsg)
                .priorityScore(sp != null ? sp.getSkillScore() : 50.0)
                .status(RecommendationStatus.SERVED)
                .servedAt(LocalDateTime.now())
                .build();

        rec = recommendationRepository.save(rec);
        return buildRecommendationResponse(rec);
    }

    @Transactional
    public void updateAfterSubmission(Long userId, Long submissionId) {
        Submission submission = submissionRepository.findById(submissionId).orElseThrow();
        skillCalculationService.calculateUserSkillProfile(userId);

        // Update Diagnostic if active
        Optional<DiagnosticAssessment> diagOpt = diagnosticRepository.findFirstByUserIdAndStatus(userId, DiagnosticAssessmentStatus.IN_PROGRESS);
        if (diagOpt.isPresent()) {
            DiagnosticAssessment diag = diagOpt.get();
            for (DiagnosticAssessmentProblem dap : diag.getProblems()) {
                if (dap.getProblem().getId().equals(submission.getProblem().getId())) {
                    dap.setCompleted(true);
                }
            }
            boolean allDone = diag.getProblems().stream().allMatch(DiagnosticAssessmentProblem::isCompleted);
            if (allDone) {
                diag.setStatus(DiagnosticAssessmentStatus.COMPLETED);
                diag.setCompletedAt(LocalDateTime.now());
            }
            diagnosticRepository.save(diag);
        }
    }

    private RecommendationReasonType determineReasonType(SkillPerformance sp, Difficulty targetDiff) {
        if (sp == null || sp.getTotalAttempts() == 0) return RecommendationReasonType.INSUFFICIENT_EVIDENCE;
        if (targetDiff.ordinal() > sp.getCurrentDifficulty().ordinal()) return RecommendationReasonType.DIFFICULTY_PROMOTION;
        if (targetDiff.ordinal() < sp.getCurrentDifficulty().ordinal()) return RecommendationReasonType.DIFFICULTY_RECOVERY;
        if (sp.getSkillScore() < 50.0) return RecommendationReasonType.WEAK_SKILL;
        return RecommendationReasonType.SKILL_BUILDING;
    }

    private AdaptiveRecommendation saveTemporaryRecommendation(User user, Problem p, RecommendationReasonType reason, String msg) {
        return recommendationRepository.save(AdaptiveRecommendation.builder()
                .user(user)
                .problem(p)
                .topic(p.getTopic())
                .difficulty(p.getDifficulty())
                .reasonType(reason)
                .reasonMessage(msg)
                .status(RecommendationStatus.SERVED)
                .build());
    }

    private AdaptiveRecommendationResponse buildRecommendationResponse(AdaptiveRecommendation rec) {
        Problem p = rec.getProblem();
        return AdaptiveRecommendationResponse.builder()
                .recommendationId(rec.getId())
                .topic(rec.getTopic())
                .difficulty(rec.getDifficulty())
                .reasonType(rec.getReasonType())
                .reason(rec.getReasonMessage())
                .skillScore(rec.getPriorityScore())
                .createdAt(rec.getCreatedAt())
                .problem(MinimalProblemResponse.builder()
                        .id(p.getId())
                        .title(p.getTitle())
                        .difficulty(p.getDifficulty())
                        .topic(p.getTopic())
                        .description(p.getDescription())
                        .build())
                .build();
    }
}
