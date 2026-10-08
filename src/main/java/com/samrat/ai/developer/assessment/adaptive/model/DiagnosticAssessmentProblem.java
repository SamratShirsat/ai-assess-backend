package com.samrat.ai.developer.assessment.adaptive.model;

import com.samrat.ai.developer.assessment.problem.model.Problem;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "diagnostic_assessment_problems")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiagnosticAssessmentProblem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id", nullable = false)
    private DiagnosticAssessment assessment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Column(nullable = false)
    private Integer sequenceNumber;

    @Builder.Default
    private boolean completed = false;
}
