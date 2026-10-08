// AdminStatisticsRepository.java
package com.samrat.ai.developer.assessment.admin.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class AdminStatisticsRepository {

    @PersistenceContext
    private final EntityManager em;

    public long countTotalUsers() {
        return em.createQuery("SELECT COUNT(u) FROM User u", Long.class).getSingleResult();
    }

    public long countTotalProblems() {
        return em.createQuery("SELECT COUNT(p) FROM Problem p", Long.class).getSingleResult();
    }

    public long countTotalSubmissions() {
        return em.createQuery("SELECT COUNT(s) FROM Submission s", Long.class).getSingleResult();
    }

    public long countAcceptedSubmissions() {
        return em.createQuery("SELECT COUNT(s) FROM Submission s WHERE s.status = 'ACCEPTED'", Long.class).getSingleResult();
    }

    public BigDecimal calculateAverageSubmissionScore() {
        Double avg = em.createQuery("SELECT AVG(s.score) FROM Submission s", Double.class).getSingleResult();
        return avg != null ? BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    public long countTotalAssessments() {
        return em.createQuery("SELECT COUNT(a) FROM Assessment a", Long.class).getSingleResult();
    }

    public long countActiveAssessments() {
        return em.createQuery("SELECT COUNT(a) FROM Assessment a WHERE a.active = true", Long.class).getSingleResult();
    }

    public long countPendingSimilarityReviews() {
        return em.createQuery("SELECT COUNT(c) FROM CodeSimilarityResult c WHERE c.status IN ('NOT_REVIEWED', 'REQUIRES_ATTENTION')", Long.class).getSingleResult();
    }

    public BigDecimal calculateAssessmentCompletionRate() {
        Long totalSessions = em.createQuery("SELECT COUNT(s) FROM AssessmentSession s", Long.class).getSingleResult();
        if (totalSessions == 0) return BigDecimal.ZERO;

        Long completedSessions = em.createQuery("SELECT COUNT(s) FROM AssessmentSession s WHERE s.status = 'COMPLETED'", Long.class).getSingleResult();
        return BigDecimal.valueOf(completedSessions)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalSessions), 2, RoundingMode.HALF_UP);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Long> getProblemsByDifficulty() {
        List<Object[]> rows = em.createQuery("SELECT p.difficulty, COUNT(p) FROM Problem p GROUP BY p.difficulty").getResultList();
        Map<String, Long> map = new HashMap<>();
        for (Object[] row : rows) {
            map.put(String.valueOf(row[0]), (Long) row[1]);
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Long> getProblemsByTopic() {
        List<Object[]> rows = em.createQuery("SELECT p.topic, COUNT(p) FROM Problem p GROUP BY p.topic").getResultList();
        Map<String, Long> map = new HashMap<>();
        for (Object[] row : rows) {
            map.put(String.valueOf(row[0]), (Long) row[1]);
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Long> getSubmissionsByStatus() {
        List<Object[]> rows = em.createQuery("SELECT s.status, COUNT(s) FROM Submission s GROUP BY s.status").getResultList();
        Map<String, Long> map = new HashMap<>();
        for (Object[] row : rows) {
            map.put(String.valueOf(row[0]), (Long) row[1]);
        }
        return map;
    }
}