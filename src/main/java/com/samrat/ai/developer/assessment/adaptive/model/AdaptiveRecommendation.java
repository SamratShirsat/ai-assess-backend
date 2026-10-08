package com.samrat.ai.developer.assessment.adaptive.model;

import com.samrat.ai.developer.assessment.problem.model.Difficulty;
import com.samrat.ai.developer.assessment.problem.model.Problem;
import com.samrat.ai.developer.assessment.problem.model.Topic;
import com.samrat.ai.developer.assessment.user.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "adaptive_recommendations", indexes = {
        @Index(name = "idx_ar_user_created", columnList = "user_id, created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdaptiveRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    // --- Fields from Adaptive Module ---
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Topic topic;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason_type", nullable = false)
    private RecommendationReasonType reasonType;

    @Column(name = "reason_message", nullable = false, columnDefinition = "TEXT")
    private String reasonMessage;

    @Column(name = "priority_score")
    private Double priorityScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RecommendationStatus status = RecommendationStatus.GENERATED;

    // --- Fields from Recommendation Module ---
    @Column(name = "recommendation_score")
    private Double recommendationScore;

    @Column(name = "previously_attempted", nullable = false)
    @Builder.Default
    private Boolean previouslyAttempted = false;

    @Column(name = "previously_solved", nullable = false)
    @Builder.Default
    private Boolean previouslySolved = false;

    @Column(name = "feedback", length = 50)
    private String feedback;

    // --- Timestamps ---
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "shown_at")
    private LocalDateTime shownAt;

    @Column(name = "served_at")
    private LocalDateTime servedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "getreason_at")
    public String getReason() {
        return this.reasonMessage;
    }
    @Column(name = "setreason_at")
    public void setReason(String reason) {
        this.reasonMessage = reason;
    }
}