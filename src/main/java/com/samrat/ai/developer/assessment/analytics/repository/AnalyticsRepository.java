package com.samrat.ai.developer.assessment.analytics.repository;

import com.samrat.ai.developer.assessment.analytics.dto.RecentActivityResponse;
import com.samrat.ai.developer.assessment.problem.model.Difficulty;
import com.samrat.ai.developer.assessment.problem.model.Topic;
import com.samrat.ai.developer.assessment.submission.model.Submission;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnalyticsRepository extends JpaRepository<Submission, Long> {

    @Query("SELECT COUNT(s) FROM Submission s WHERE s.user.id = :userId AND s.status != 'SYSTEM_ERROR'")
    long countTotalSubmissions(@Param("userId") Long userId);

    @Query("SELECT COUNT(s) FROM Submission s WHERE s.user.id = :userId AND s.status = 'ACCEPTED'")
    long countAcceptedSubmissions(@Param("userId") Long userId);

    @Query("SELECT COUNT(DISTINCT s.problem.id) FROM Submission s WHERE s.user.id = :userId AND s.status != 'SYSTEM_ERROR'")
    long countTotalProblemsAttempted(@Param("userId") Long userId);

    @Query("SELECT COUNT(DISTINCT s.problem.id) FROM Submission s WHERE s.user.id = :userId AND s.status = 'ACCEPTED'")
    long countTotalProblemsSolved(@Param("userId") Long userId);

    @Query("SELECT AVG(s.score) FROM Submission s WHERE s.user.id = :userId AND s.status != 'SYSTEM_ERROR'")
    Double getAverageScoreForUser(@Param("userId") Long userId);

    @Query("SELECT COUNT(s) FROM Submission s WHERE s.user.id = :userId AND s.problem.topic = :topic AND s.status != 'SYSTEM_ERROR'")
    long countSubmissionsByTopic(@Param("userId") Long userId, @Param("topic") Topic topic);

    @Query("SELECT COUNT(DISTINCT s.problem.id) FROM Submission s WHERE s.user.id = :userId AND s.problem.topic = :topic AND s.status = 'ACCEPTED'")
    long countSolvedProblemsByTopic(@Param("userId") Long userId, @Param("topic") Topic topic);

    @Query("SELECT AVG(s.score) FROM Submission s WHERE s.user.id = :userId AND s.problem.topic = :topic AND s.status != 'SYSTEM_ERROR'")
    Double getAverageScoreByTopic(@Param("userId") Long userId, @Param("topic") Topic topic);

    @Query("SELECT COUNT(s) FROM Submission s WHERE s.user.id = :userId AND s.problem.difficulty = :difficulty AND s.status != 'SYSTEM_ERROR'")
    long countSubmissionsByDifficulty(@Param("userId") Long userId, @Param("difficulty") Difficulty difficulty);

    @Query("SELECT COUNT(DISTINCT s.problem.id) FROM Submission s WHERE s.user.id = :userId AND s.problem.difficulty = :difficulty AND s.status = 'ACCEPTED'")
    long countSolvedProblemsByDifficulty(@Param("userId") Long userId, @Param("difficulty") Difficulty difficulty);

    @Query("SELECT AVG(s.score) FROM Submission s WHERE s.user.id = :userId AND s.problem.difficulty = :difficulty AND s.status != 'SYSTEM_ERROR'")
    Double getAverageScoreByDifficulty(@Param("userId") Long userId, @Param("difficulty") Difficulty difficulty);

    @Query("SELECT s FROM Submission s WHERE s.user.id = :userId AND s.status != 'SYSTEM_ERROR' ORDER BY s.submittedAt DESC")
    List<Submission> findRecentSubmissions(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT s FROM Submission s WHERE s.user.id = :userId AND s.problem.topic = :topic AND s.status != 'SYSTEM_ERROR' ORDER BY s.submittedAt DESC")
    List<Submission> findRecentSubmissionsByTopic(@Param("userId") Long userId, @Param("topic") Topic topic, Pageable pageable);

    @Query("""
        SELECT new com.samrat.ai.developer.assessment.analytics.dto.RecentActivityResponse(
            s.id, s.problem.id, s.problem.title, s.problem.topic, s.problem.difficulty, s.status, s.score, s.submittedAt
        )
        FROM Submission s
        WHERE s.user.id = :userId
        ORDER BY s.submittedAt DESC
    """)
    List<RecentActivityResponse> findRecentActivities(@Param("userId") Long userId, Pageable pageable);
}