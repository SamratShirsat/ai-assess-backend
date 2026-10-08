CREATE TABLE IF NOT EXISTS performance_trends (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    snapshot_date DATE NOT NULL,
    overall_score NUMERIC(6, 2) NOT NULL,
    problems_solved INT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_date UNIQUE (user_id, snapshot_date)
);