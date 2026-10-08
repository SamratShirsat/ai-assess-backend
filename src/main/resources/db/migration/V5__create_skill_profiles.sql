CREATE TABLE IF NOT EXISTS user_skill_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    overall_score NUMERIC(6, 2) NOT NULL DEFAULT 0.0,
    overall_level VARCHAR(50) NOT NULL DEFAULT 'NOVICE',
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS skill_performances (
    id BIGSERIAL PRIMARY KEY,
    user_skill_profile_id BIGINT NOT NULL REFERENCES user_skill_profiles(id) ON DELETE CASCADE,
    topic VARCHAR(50) NOT NULL,
    skill_score NUMERIC(6, 2) NOT NULL DEFAULT 0.0,
    skill_level VARCHAR(50) NOT NULL DEFAULT 'NOVICE',
    attempts INT NOT NULL DEFAULT 0,
    accepted_attempts INT NOT NULL DEFAULT 0,
    success_rate NUMERIC(5, 2) NOT NULL DEFAULT 0.0,
    average_score NUMERIC(5, 2) NOT NULL DEFAULT 0.0,
    recent_success_rate NUMERIC(5, 2) NOT NULL DEFAULT 0.0,
    average_execution_time_ms BIGINT DEFAULT 0,
    consistency NUMERIC(5, 2) NOT NULL DEFAULT 0.0,
    current_difficulty VARCHAR(50) NOT NULL DEFAULT 'EASY',
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_profile_topic UNIQUE (user_skill_profile_id, topic)
);