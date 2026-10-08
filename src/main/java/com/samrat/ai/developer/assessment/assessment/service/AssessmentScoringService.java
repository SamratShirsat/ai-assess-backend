package com.samrat.ai.developer.assessment.assessment.service;

import com.samrat.ai.developer.assessment.adaptive.service.SkillCalculationService;
import com.samrat.ai.developer.assessment.assessment.dto.AssessmentResultResponse;
import com.samrat.ai.developer.assessment.assessment.model.AssessmentProblemStatus;
import com.samrat.ai.developer.assessment.assessment.model.AssessmentSession;
import com.samrat.ai.developer.assessment.assessment.model.AssessmentSessionProblem;
import com.samrat.ai.developer.assessment.assessment.model.AssessmentStatus;
import com.samrat.ai.developer.assessment.assessment.repository.AssessmentSessionProblemRepository;
import com.samrat.ai.developer.assessment.assessment.repository.AssessmentSessionRepository;
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
public class AssessmentScoringService {

    private final AssessmentSessionRepository sessionRepository;
    private final AssessmentSessionProblemRepository sessionProblemRepository;
    private final SkillCalculationService skillCalculationService;
    private final Clock clock = Clock.systemUTC();

    @Transactional
    public AssessmentSession finalizeSession(Long sessionId) {
        AssessmentSession session = sessionRepository.findByIdWithLock(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found with id: " + sessionId));

        // Idempotency check: avoid double finalization
        if (session.getStatus() == AssessmentStatus.COMPLETED || session.getStatus() == AssessmentStatus.EXPIRED) {
            return session;
        }

        Instant now = Instant.now(clock);
        boolean isExpired = session.isExpired(now);
        session.setStatus(isExpired ? AssessmentStatus.EXPIRED : AssessmentStatus.COMPLETED);
        session.setSubmittedAt(now);

        recalculateCurrentScore(session);

        // Update skill engine & recommendations
        try {
            skillCalculationService.updateProfileForUser(session.getUser().getId());
            log.info("Skill analysis updated after assessment session: {}", session.getId());
        } catch (Exception e) {
            log.error("Failed to update skill profile after assessment {}: {}", sessionId, e.getMessage());
        }

        return sessionRepository.save(session);
    }

    public void recalculateCurrentScore(AssessmentSession session) {
        List<AssessmentSessionProblem> sessionProblems = sessionProblemRepository
                .findAllBySessionIdOrderByAssessmentProblemDisplayOrderAsc(session.getId());

        BigDecimal totalEarned = sessionProblems.stream()
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
                .orElseThrow(() -> new IllegalArgumentException("Session not found with id: " + sessionId));

        if (!session.getUser().getId().equals(userId)) {
            throw new SecurityException("Unauthorized access to assessment result.");
        }

        List<AssessmentSessionProblem> problems = sessionProblemRepository
                .findAllBySessionIdOrderByAssessmentProblemDisplayOrderAsc(sessionId);

        int attempted = (int) problems.stream().filter(p -> p.getAttemptCount() > 0).count();
        int solved = (int) problems.stream().filter(p -> p.getStatus() == AssessmentProblemStatus.COMPLETED).count();

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
                attempted,
                solved
        );
    }
}
