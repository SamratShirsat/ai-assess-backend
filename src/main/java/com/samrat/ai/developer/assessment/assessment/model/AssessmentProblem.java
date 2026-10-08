package com.samrat.ai.developer.assessment.assessment.model;

import com.samrat.ai.developer.assessment.problem.model.Problem;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "assessment_problems",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_assessment_problem", columnNames = {"assessment_id", "problem_id"}),
                @UniqueConstraint(name = "uk_assessment_order", columnNames = {"assessment_id", "display_order"})
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssessmentProblem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(nullable = false)
    private Integer points;

    @Column(nullable = false)
    private Boolean required;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}