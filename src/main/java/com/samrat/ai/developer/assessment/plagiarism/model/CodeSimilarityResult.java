package com.samrat.ai.developer.assessment.plagiarism.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "code_similarity_results")
public class CodeSimilarityResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "submission_a_id", nullable = false)
    private Long submissionAId;

    @Column(name = "submission_b_id", nullable = false)
    private Long submissionBId;

    @Column(name = "problem_id", nullable = false)
    private Long problemId;

    @Column(name = "similarity_score", nullable = false)
    private Double similarityScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "similarity_level", nullable = false)
    private SimilarityLevel similarityLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "confidence", nullable = false)
    private SimilarityConfidence confidence;

    @Column(name = "matched_features", columnDefinition = "TEXT")
    private String matchedFeatures; // Store as comma-separated or JSON string

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SimilarityStatus status = SimilarityStatus.NOT_REVIEWED;

    @Column(name = "submission_low_id", nullable = false)
    private Long submissionLowId;

    @Column(name = "submission_high_id", nullable = false)
    private Long submissionHighId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    public CodeSimilarityResult() {}

    @PrePersist
    @PreUpdate
    public void normalizePair() {
        if (submissionAId != null && submissionBId != null) {
            this.submissionLowId = Math.min(submissionAId, submissionBId);
            this.submissionHighId = Math.max(submissionAId, submissionBId);
        }
        this.updatedAt = OffsetDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSubmissionAId() { return submissionAId; }
    public void setSubmissionAId(Long submissionAId) { this.submissionAId = submissionAId; }
    public Long getSubmissionBId() { return submissionBId; }
    public void setSubmissionBId(Long submissionBId) { this.submissionBId = submissionBId; }
    public Long getProblemId() { return problemId; }
    public void setProblemId(Long problemId) { this.problemId = problemId; }
    public Double getSimilarityScore() { return similarityScore; }
    public void setSimilarityScore(Double similarityScore) { this.similarityScore = similarityScore; }
    public SimilarityLevel getSimilarityLevel() { return similarityLevel; }
    public void setSimilarityLevel(SimilarityLevel similarityLevel) { this.similarityLevel = similarityLevel; }
    public SimilarityConfidence getConfidence() { return confidence; }
    public void setConfidence(SimilarityConfidence confidence) { this.confidence = confidence; }
    public String getMatchedFeatures() { return matchedFeatures; }
    public void setMatchedFeatures(String matchedFeatures) { this.matchedFeatures = matchedFeatures; }
    public SimilarityStatus getStatus() { return status; }
    public void setStatus(SimilarityStatus status) { this.status = status; }
    public Long getSubmissionLowId() { return submissionLowId; }
    public Long getSubmissionHighId() { return submissionHighId; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}