-- Create Enum-like VARCHAR fields or domain tables for similarity level, confidence, and review status
CREATE TABLE IF NOT EXISTS code_similarity_results (
    id BIGSERIAL PRIMARY KEY,
    submission_a_id BIGINT NOT NULL,
    submission_b_id BIGINT NOT NULL,
    problem_id BIGINT NOT NULL,
    similarity_score DOUBLE PRECISION NOT NULL,
    similarity_level VARCHAR(30) NOT NULL,
    confidence VARCHAR(30) NOT NULL,
    matched_features TEXT, -- JSON or comma-separated string representation
    status VARCHAR(30) NOT NULL DEFAULT 'NOT_REVIEWED',
    submission_low_id BIGINT NOT NULL,
    submission_high_id BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,

    CONSTRAINT fk_similarity_submission_a FOREIGN KEY (submission_a_id) REFERENCES submissions(id) ON DELETE CASCADE,
    CONSTRAINT fk_similarity_submission_b FOREIGN KEY (submission_b_id) REFERENCES submissions(id) ON DELETE CASCADE,
    CONSTRAINT fk_similarity_problem FOREIGN KEY (problem_id) REFERENCES problems(id) ON DELETE CASCADE,
    CONSTRAINT uk_similarity_normalized_pair UNIQUE (submission_low_id, submission_high_id)
);

-- Performance Indexes
CREATE INDEX idx_similarity_problem_id ON code_similarity_results(problem_id);
CREATE INDEX idx_similarity_sub_a ON code_similarity_results(submission_a_id);
CREATE INDEX idx_similarity_sub_b ON code_similarity_results(submission_b_id);
CREATE INDEX idx_similarity_score ON code_similarity_results(similarity_score);
CREATE INDEX idx_similarity_created_at ON code_similarity_results(created_at);
CREATE INDEX idx_similarity_status ON code_similarity_results(status);