package com.samrat.ai.developer.assessment.adaptive.model;

import com.samrat.ai.developer.assessment.problem.model.Difficulty;
import com.samrat.ai.developer.assessment.problem.model.Topic;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "skill_performances", indexes = {
        @Index(name = "idx_sp_profile_topic", columnList = "user_skill_profile_id, topic")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillPerformance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_skill_profile_id", nullable = false)
    private UserSkillProfile userSkillProfile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Topic topic;

    private Integer skillScore;

    @Enumerated(EnumType.STRING)
    private SkillLevel skillLevel;

    @Builder.Default
    private Integer totalAttempts = 0;

    @Builder.Default
    private Integer acceptedAttempts = 0;

    @Builder.Default
    private Double successRate = 0.0;

    @Builder.Default
    private Double averageScore = 0.0;

    @Builder.Default
    private Integer easyAttempts = 0;
    @Builder.Default
    private Double easySuccessRate = 0.0;

    @Builder.Default
    private Integer mediumAttempts = 0;
    @Builder.Default
    private Double mediumSuccessRate = 0.0;

    @Builder.Default
    private Integer hardAttempts = 0;
    @Builder.Default
    private Double hardSuccessRate = 0.0;

    @Builder.Default
    private Double averageExecutionTimeMs = 0.0;

    @Builder.Default
    private Double recentSuccessRate = 0.0;

    @Builder.Default
    private Double consistencyScore = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Difficulty currentDifficulty = Difficulty.EASY;

    private LocalDateTime lastAttemptAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
