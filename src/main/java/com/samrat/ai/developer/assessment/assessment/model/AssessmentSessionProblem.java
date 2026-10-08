package com.samrat.ai.developer.assessment.assessment.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "assessment_session_problems",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_session_assessment_problem", columnNames = {"session_id", "assessment_problem_id"})
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssessmentSessionProblem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private AssessmentSession session;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessment_problem_id", nullable = false)
    private AssessmentProblem assessmentProblem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AssessmentProblemStatus status;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "best_submission_id")
    private Long bestSubmissionId;

    @Column(precision = 6, scale = 2)
    private BigDecimal score;

    @Column(name = "attempt_count", nullable = false)
    private Integer attemptCount;

    @Column(name = "last_saved_code", columnDefinition = "TEXT")
    private String lastSavedCode;

    @Column(name = "last_saved_language", length = 32)
    private String lastSavedLanguage;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}