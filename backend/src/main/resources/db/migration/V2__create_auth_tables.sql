-- 1. Alter users table for authentication version and password change timestamp
ALTER TABLE users
    ADD COLUMN auth_version INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN password_changed_at TIMESTAMPTZ NULL;

-- 2. Create refresh_tokens table
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    token_family_id UUID NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_used_at TIMESTAMPTZ NULL,
    revoked_at TIMESTAMPTZ NULL,
    revoke_reason VARCHAR(100) NULL,
    replaced_by_token_id UUID NULL,
    created_ip VARCHAR(45) NULL,
    user_agent VARCHAR(1000) NULL,
    request_id VARCHAR(100) NULL,
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_refresh_tokens_replaced_by FOREIGN KEY (replaced_by_token_id) REFERENCES refresh_tokens(id) ON DELETE SET NULL
);

-- Indexes for refresh_tokens
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_family_id ON refresh_tokens (token_family_id);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens (expires_at);
CREATE INDEX idx_refresh_tokens_revoked_at ON refresh_tokens (revoked_at);

-- 3. Create authentication_events table (append-only audit log)
CREATE TABLE authentication_events (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NULL,
    email_normalized VARCHAR(320) NULL,
    event_type VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL,
    failure_reason VARCHAR(100) NULL,
    ip_address VARCHAR(45) NULL,
    user_agent VARCHAR(1000) NULL,
    request_id VARCHAR(100) NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_auth_events_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Indexes for authentication_events
CREATE INDEX idx_auth_events_user_id ON authentication_events (user_id);
CREATE INDEX idx_auth_events_email_normalized ON authentication_events (email_normalized);
CREATE INDEX idx_auth_events_created_at ON authentication_events (created_at);
