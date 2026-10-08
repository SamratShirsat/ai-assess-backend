package com.samrat.ai.developer.assessment.adaptive.model;

import com.samrat.ai.developer.assessment.user.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_skill_profiles", indexes = {
        @Index(name = "idx_usp_user_id", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSkillProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private Double overallScore;

    @Enumerated(EnumType.STRING)
    private SkillLevel overallLevel;

    private LocalDateTime lastCalculatedAt;

    @Builder.Default
    @OneToMany(mappedBy = "userSkillProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SkillPerformance> skillPerformances = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
