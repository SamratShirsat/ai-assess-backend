package com.samrat.ai.developer.assessment.assessment.repository;

import com.samrat.ai.developer.assessment.assessment.model.AssessmentSession;
import com.samrat.ai.developer.assessment.assessment.model.AssessmentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface AssessmentSessionRepository extends JpaRepository<AssessmentSession, Long> {

    @Query("SELECT s FROM AssessmentSession s WHERE s.user.id = :userId AND s.assessment.id = :assessmentId AND s.status IN ('STARTED', 'IN_PROGRESS')")
    Optional<AssessmentSession> findActiveSession(@Param("userId") Long userId, @Param("assessmentId") Long assessmentId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM AssessmentSession s WHERE s.id = :id")
    Optional<AssessmentSession> findByIdWithLock(@Param("id") Long id);

    Page<AssessmentSession> findAllByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Query("SELECT s FROM AssessmentSession s WHERE s.status = 'IN_PROGRESS' AND s.expiresAt <= :cutoff")
    List<AssessmentSession> findExpiredInProgressSessions(@Param("cutoff") Instant cutoff);
}