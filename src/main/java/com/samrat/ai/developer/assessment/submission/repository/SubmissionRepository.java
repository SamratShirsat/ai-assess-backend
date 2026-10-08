package com.samrat.ai.developer.assessment.submission.repository;

import com.samrat.ai.developer.assessment.submission.model.Submission;
import com.samrat.ai.developer.assessment.submission.model.SubmissionStatus;
import com.samrat.ai.developer.assessment.execution.model.SupportedLanguage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    Page<Submission> findByUserIdOrderBySubmittedAtDesc(Long userId, Pageable pageable);
    Page<Submission> findByUserIdAndProblemIdOrderBySubmittedAtDesc(Long userId, Long problemId, Pageable pageable);
    Optional<Submission> findByIdAndUserId(Long id, Long userId);

    long countByUserId(Long id);

    long countByUserIdAndStatus(Long userId, SubmissionStatus submissionStatus);

    @Query("select avg(s.score) from Submission s where s.user.id = :userId")
    Double findAverageScoreByUserId(@Param("userId") Long userId);

    Page<Submission> findAllByStatus(SubmissionStatus status, Pageable pageable);

    Page<Submission> findAllByLanguage(SupportedLanguage language, Pageable pageable);

    @Query("select distinct s.problem.id from Submission s where s.user.id = :userId and s.status = :status")
    java.util.List<Long> findSolvedProblemIdsByUserId(@Param("userId") Long userId, @Param("status") SubmissionStatus status);

    @Query("select distinct s.problem.id from Submission s where s.user.id = :userId")
    java.util.List<Long> findAllAttemptedProblemIdsByUserId(@Param("userId") Long userId);

    @Query("select s.problem.id from Submission s where s.user.id = :userId order by s.submittedAt desc")
    java.util.List<Long> findRecentAttemptedProblemIdsByUserId(@Param("userId") Long userId, Pageable pageable);
}
