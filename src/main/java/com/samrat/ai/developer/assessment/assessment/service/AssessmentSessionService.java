package com.samrat.ai.developer.assessment.assessment.service;

import com.samrat.ai.developer.assessment.adaptive.service.SkillCalculationService;
import com.samrat.ai.developer.assessment.assessment.dto.*;
import com.samrat.ai.developer.assessment.assessment.dto.AssessmentSessionResponse.SessionProblemOverview;
import com.samrat.ai.developer.assessment.assessment.exception.*;
import com.samrat.ai.developer.assessment.assessment.model.*;
import com.samrat.ai.developer.assessment.assessment.repository.*;
import com.samrat.ai.developer.assessment.submission.dto.SubmissionDTOs.CodeSubmissionRequest;
import com.samrat.ai.developer.assessment.submission.dto.SubmissionDTOs.CodeSubmissionResponse;
import com.samrat.ai.developer.assessment.execution.model.SupportedLanguage;
import com.samrat.ai.developer.assessment.submission.model.SubmissionStatus;
import com.samrat.ai.developer.assessment.submission.service.SubmissionEvaluationService;
import com.samrat.ai.developer.assessment.user.model.User;
import com.samrat.ai.developer.assessment.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssessmentSessionService {

    private final AssessmentRepository assessmentRepository;
    private final AssessmentProblemRepository assessmentProblemRepository;
    private final AssessmentSessionRepository sessionRepository;
    private final AssessmentSessionProblemRepository sessionProblemRepository;
    private final UserRepository userRepository;
    private final SubmissionEvaluationService evaluationService;
    private final SkillCalculationService skillCalculationService;
    private final Clock clock = Clock.systemUTC();

    @Transactional
    public StartAssessmentResponse startAssessment(Long assessmentId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        Assessment assessment = assessmentRepository.findByIdAndActiveTrue(assessmentId)
                .orElseThrow(() -> new AssessmentNotFoundException("Active assessment not found: " + assessmentId));

        var existing = sessionRepository.findActiveSession(userId, assessmentId);
        if (existing.isPresent()) {
            AssessmentSession session = existing.get();
            Instant now = Instant.now(clock);
            if (session.isExpired(now)) {
                finalizeSession(session.getId());
            } else {
                return new StartAssessmentResponse(
                        session.getId(),
                        assessment.getId(),
                        session.getStatus(),
                        session.getStartedAt(),
                        session.getExpiresAt(),
                        now,
                        session.getSessionProblems().size(),
                        session.getCurrentProblemId()
                );
            }
        }

        List<AssessmentProblem> assessmentProblems = assessmentProblemRepository.findAllByAssessmentIdOrderByDisplayOrderAsc(assessmentId);
        if (assessmentProblems.isEmpty()) {
            throw new InvalidAssessmentStateException("Assessment does not have any assigned problems.");
        }

        Instant now = Instant.now(clock);
        Instant expiresAt = now.plus(Duration.ofMinutes(assessment.getDurationMinutes()));
        Long initialProblemId = assessmentProblems.get(0).getProblem().getId();

        AssessmentSession session = AssessmentSession.builder()
                .assessment(assessment)
                .user(user)
                .status(AssessmentStatus.IN_PROGRESS)
                .startedAt(now)
                .expiresAt(expiresAt)
                .lastActivityAt(now)
                .totalScore(BigDecimal.ZERO)
                .percentage(BigDecimal.ZERO)
                .passed(false)
                .currentProblemId(initialProblemId)
                .build();

        AssessmentSession savedSession = sessionRepository.save(session);

        List<AssessmentSessionProblem> sessionProblems = assessmentProblems.stream().map(ap ->
                AssessmentSessionProblem.builder()
                        .session(savedSession)
                        .assessmentProblem(ap)
                        .status(AssessmentProblemStatus.NOT_STARTED)
                        .attemptCount(0)
                        .score(BigDecimal.ZERO)
                        .lastSavedCode(ap.getProblem().getStarterCode())
                        .lastSavedLanguage("JAVA")
                        .build()
        ).toList();

        sessionProblemRepository.saveAll(sessionProblems);
        savedSession.setSessionProblems(sessionProblems);

        log.info("Started assessment session id={} for user={}", savedSession.getId(), userId);

        return new StartAssessmentResponse(
                savedSession.getId(),
                assessment.getId(),
                savedSession.getStatus(),
                savedSession.getStartedAt(),
                savedSession.getExpiresAt(),
                now,
                sessionProblems.size(),
                initialProblemId
        );
    }

    @Transactional
    public AssessmentSessionResponse getSession(Long sessionId, Long userId) {
        AssessmentSession session = validateOwnershipAndState(sessionId, userId);
        Instant now = Instant.now(clock);

        List<SessionProblemOverview> problemOverviews = sessionProblemRepository
                .findAllBySessionIdOrderByAssessmentProblemDisplayOrderAsc(sessionId)
                .stream()
                .map(p -> new SessionProblemOverview(
                        p.getAssessmentProblem().getProblem().getId(),
                        p.getAssessmentProblem().getProblem().getTitle(),
                        p.getAssessmentProblem().getDisplayOrder(),
                        p.getAssessmentProblem().getPoints(),
                        p.getStatus(),
                        p.getScore(),
                        p.getAttemptCount()
                )).toList();

        return new AssessmentSessionResponse(
                session.getId(),
                session.getAssessment().getId(),
                session.getAssessment().getTitle(),
                session.getStatus(),
                session.getStartedAt(),
                session.getExpiresAt(),
                now,
                session.getCurrentProblemId(),
                session.getTotalScore(),
                session.getPercentage(),
                problemOverviews
        );
    }

    @Transactional(readOnly = true)
    public AssessmentTimeResponse getSessionTime(Long sessionId, Long userId) {
        AssessmentSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new AssessmentSessionNotFoundException("Session not found: " + sessionId));

        if (!session.getUser().getId().equals(userId)) {
            throw new AssessmentAccessDeniedException("Unauthorized session access");
        }

        Instant now = Instant.now(clock);
        long remaining = Math.max(0, Duration.between(now, session.getExpiresAt()).getSeconds());
        return new AssessmentTimeResponse(now, session.getExpiresAt(), session.getStatus(), remaining);
    }

    @Transactional
    public SessionProblemDetailResponse getSessionProblem(Long sessionId, Long problemId, Long userId) {
        AssessmentSession session = validateOwnershipAndState(sessionId, userId);

        AssessmentSessionProblem asp = sessionProblemRepository.findBySessionIdAndProblemId(sessionId, problemId)
                .orElseThrow(() -> new IllegalArgumentException("Problem " + problemId + " does not belong to session " + sessionId));

        session.setCurrentProblemId(problemId);
        session.setLastActivityAt(Instant.now(clock));
        sessionRepository.save(session);

        var p = asp.getAssessmentProblem().getProblem();
        return new SessionProblemDetailResponse(
                p.getId(),
                p.getTitle(),
                p.getDescription(),
                p.getDifficulty(),
                p.getTopic(),
                p.getConstraints(),
                p.getInputFormat(),
                p.getOutputFormat(),
                p.getStarterCode(),
                asp.getAssessmentProblem().getPoints(),
                asp.getAssessmentProblem().getDisplayOrder(),
                asp.getStatus(),
                asp.getLastSavedCode(),
                asp.getLastSavedLanguage(),
                asp.getAttemptCount()
        );
    }

    @Transactional
    public void saveDraft(Long sessionId, Long problemId, DraftSaveRequest req, Long userId) {
        AssessmentSession session = validateOwnershipAndState(sessionId, userId);
        Instant now = Instant.now(clock);

        AssessmentSessionProblem asp = sessionProblemRepository.findBySessionIdAndProblemId(sessionId, problemId)
                .orElseThrow(() -> new IllegalArgumentException("Problem " + problemId + " does not belong to session"));

        asp.setLastSavedCode(req.sourceCode());
        asp.setLastSavedLanguage(req.language());
        if (asp.getStatus() == AssessmentProblemStatus.NOT_STARTED) {
            asp.setStatus(AssessmentProblemStatus.IN_PROGRESS);
            asp.setStartedAt(now);
        }
        session.setLastActivityAt(now);
        sessionProblemRepository.save(asp);
        sessionRepository.save(session);
    }

    @Transactional
    public CodeSubmissionResponse submitAssessmentCode(Long sessionId, Long problemId, AssessmentSubmissionRequest req, Long userId) {
        AssessmentSession session = sessionRepository.findByIdWithLock(sessionId)
                .orElseThrow(() -> new AssessmentSessionNotFoundException("Session not found: " + sessionId));

        if (!session.getUser().getId().equals(userId)) {
            throw new AssessmentAccessDeniedException("Unauthorized assessment session access.");
        }

        Instant now = Instant.now(clock);
        if (session.isExpired(now)) {
            finalizeSession(sessionId);
            throw new AssessmentExpiredException("Assessment session has expired. Submissions are no longer accepted.");
        }

        if (session.getStatus() != AssessmentStatus.IN_PROGRESS) {
            throw new InvalidAssessmentStateException("Assessment is not in progress. Current status: " + session.getStatus());
        }

        AssessmentSessionProblem asp = sessionProblemRepository.findBySessionIdAndProblemId(sessionId, problemId)
                .orElseThrow(() -> new IllegalArgumentException("Problem " + problemId + " does not belong to session."));

        asp.setLastSavedCode(req.sourceCode());
        asp.setLastSavedLanguage(req.language());
        asp.setAttemptCount(asp.getAttemptCount() + 1);

        CodeSubmissionRequest judgeReq = new CodeSubmissionRequest();
        judgeReq.setProblemId(problemId);
        judgeReq.setLanguage(SupportedLanguage.valueOf(req.language().toUpperCase()));
        judgeReq.setSourceCode(req.sourceCode());

        CodeSubmissionResponse subResult = evaluationService.evaluate(judgeReq, session.getUser().getEmail());

        BigDecimal earnedFraction = BigDecimal.valueOf(subResult.getPassedTests())
                .divide(BigDecimal.valueOf(Math.max(1, subResult.getTotalTests())), 4, RoundingMode.HALF_UP);
        BigDecimal earnedPoints = BigDecimal.valueOf(asp.getAssessmentProblem().getPoints()).multiply(earnedFraction);

        if (asp.getScore() == null || earnedPoints.compareTo(asp.getScore()) > 0) {
            asp.setScore(earnedPoints);
            asp.setBestSubmissionId(subResult.getSubmissionId());
        }

        if (subResult.getStatus() == SubmissionStatus.ACCEPTED) {
            asp.setStatus(AssessmentProblemStatus.COMPLETED);
            asp.setCompletedAt(now);
        } else {
            asp.setStatus(AssessmentProblemStatus.SUBMITTED);
        }

        sessionProblemRepository.save(asp);
        session.setLastActivityAt(now);
        sessionRepository.save(session);

        recalculateCurrentScore(session);

        return subResult;
    }

    @Transactional
    public CodeSubmissionResponse processAssessmentSubmission(Long sessionId, Long problemId, Long userId, AssessmentSubmissionRequest request) {
        return submitAssessmentCode(sessionId, problemId, request, userId);
    }

    @Transactional
    public AssessmentResultResponse finishAssessment(Long sessionId, Long userId) {
        finalizeSession(sessionId);
        return getResult(sessionId, userId);
    }

    @Transactional
    public AssessmentSession finalizeSession(Long sessionId) {
        AssessmentSession session = sessionRepository.findByIdWithLock(sessionId)
                .orElseThrow(() -> new AssessmentSessionNotFoundException("Session not found: " + sessionId));

        if (session.getStatus() == AssessmentStatus.COMPLETED || session.getStatus() == AssessmentStatus.EXPIRED) {
            return session;
        }

        Instant now = Instant.now(clock);
        boolean wasExpired = session.isExpired(now);
        session.setStatus(wasExpired ? AssessmentStatus.EXPIRED : AssessmentStatus.COMPLETED);
        session.setSubmittedAt(now);

        recalculateCurrentScore(session);

        try {
            skillCalculationService.updateProfileForUser(session.getUser().getId());
            log.info("Skill profile updated after assessment session id={}", session.getId());
        } catch (Exception e) {
            log.error("Failed to update skill profile after assessment {}: {}", sessionId, e.getMessage());
        }

        return sessionRepository.save(session);
    }

    public void recalculateCurrentScore(AssessmentSession session) {
        List<AssessmentSessionProblem> problems = sessionProblemRepository
                .findAllBySessionIdOrderByAssessmentProblemDisplayOrderAsc(session.getId());

        BigDecimal totalEarned = problems.stream()
                .map(p -> p.getScore() != null ? p.getScore() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int maxPoints = session.getAssessment().getTotalPoints();
        BigDecimal percentage = maxPoints > 0
                ? totalEarned.multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(maxPoints), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        boolean passed = percentage.compareTo(session.getAssessment().getPassingScore()) >= 0;

        session.setTotalScore(totalEarned);
        session.setPercentage(percentage);
        session.setPassed(passed);
    }

    @Transactional(readOnly = true)
    public AssessmentResultResponse getResult(Long sessionId, Long userId) {
        AssessmentSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new AssessmentSessionNotFoundException("Session not found: " + sessionId));

        if (!session.getUser().getId().equals(userId)) {
            throw new AssessmentAccessDeniedException("Unauthorized access to assessment result.");
        }

        List<AssessmentSessionProblem> problems = sessionProblemRepository
                .findAllBySessionIdOrderByAssessmentProblemDisplayOrderAsc(sessionId);

        long attempted = problems.stream().filter(p -> p.getAttemptCount() > 0).count();
        long solved = problems.stream().filter(p -> p.getStatus() == AssessmentProblemStatus.COMPLETED).count();

        Instant end = session.getSubmittedAt() != null ? session.getSubmittedAt() : session.getExpiresAt();
        long duration = Duration.between(session.getStartedAt(), end).getSeconds();

        return new AssessmentResultResponse(
                session.getId(),
                session.getAssessment().getId(),
                session.getAssessment().getTitle(),
                session.getStatus(),
                session.getTotalScore(),
                session.getAssessment().getTotalPoints(),
                session.getPercentage(),
                session.getPassed(),
                session.getStartedAt(),
                session.getSubmittedAt(),
                duration,
                (int) attempted,
                (int) solved
        );
    }

    public AssessmentSession validateOwnershipAndState(Long sessionId, Long userId) {
        AssessmentSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new AssessmentSessionNotFoundException("Session not found: " + sessionId));

        if (!session.getUser().getId().equals(userId)) {
            throw new AssessmentAccessDeniedException("Access denied to session: " + sessionId);
        }

        Instant now = Instant.now(clock);
        if (session.getStatus() == AssessmentStatus.IN_PROGRESS && session.isExpired(now)) {
            log.info("Session {} expired at {}. Finalizing.", sessionId, session.getExpiresAt());
            return finalizeSession(sessionId);
        }

        return session;
    }
}
