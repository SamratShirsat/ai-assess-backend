package com.samrat.ai.developer.assessment.problem.repository;

import com.samrat.ai.developer.assessment.problem.model.Difficulty;
import com.samrat.ai.developer.assessment.problem.model.Problem;
import com.samrat.ai.developer.assessment.problem.model.Topic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProblemRepository extends JpaRepository<Problem, Long> {

    @Query("""
        SELECT p FROM Problem p WHERE 
            (:difficulty IS NULL OR p.difficulty = :difficulty) AND 
            (:topic IS NULL OR p.topic = :topic) AND 
            (CAST(:search AS string) IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
    """)
    Page<Problem> findWithFilters(
            @Param("difficulty") Difficulty difficulty,
            @Param("topic") Topic topic,
            @Param("search") String search,
            Pageable pageable
    );

    boolean existsByTitle(String title);

    List<Problem> findByTopicAndDifficulty(Topic topic, Difficulty difficulty);

    List<Problem> findByTopic(Topic topic);
}