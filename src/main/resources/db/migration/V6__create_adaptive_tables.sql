CREATE TABLE IF NOT EXISTS adaptive_recommendations (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    problem_id BIGINT NOT NULL REFERENCES problems(id) ON DELETE CASCADE,
    topic VARCHAR(50) NOT NULL,
    difficulty VARCHAR(50) NOT NULL,
    reason TEXT NOT NULL,
    priority_score NUMERIC(6, 2) NOT NULL DEFAULT 0.0,
    status VARCHAR(50) NOT NULL DEFAULT 'GENERATED',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    served_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_recommendations_user_status ON adaptive_recommendations(user_id, status);