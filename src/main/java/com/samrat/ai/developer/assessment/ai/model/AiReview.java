package com.samrat.ai.developer.assessment.ai.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "ai_reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "submission_id")
    private Long submissionId;

    @Column(name = "problem_id", nullable = false)
    private Long problemId;

    @Column(name = "language", nullable = false)
    private String language;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "review_json", nullable = false, columnDefinition = "TEXT")
    private String reviewJson;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }
}