package com.samrat.ai.developer.assessment.ai.model;

import com.samrat.ai.developer.assessment.ai.model.HintLevel;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "ai_hints")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiHint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "problem_id", nullable = false)
    private Long problemId;

    @Enumerated(EnumType.STRING)
    @Column(name = "hint_level", nullable = false)
    private HintLevel hintLevel;

    @Column(name = "hint_text", nullable = false, columnDefinition = "TEXT")
    private String hintText;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }
}