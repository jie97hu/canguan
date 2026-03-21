CREATE TABLE IF NOT EXISTS auth_refresh_token (
    id              CHAR(32)     NOT NULL PRIMARY KEY,
    user_id         BIGINT       NOT NULL,
    token_hash      CHAR(64)     NOT NULL,
    expires_at      DATETIME     NOT NULL,
    revoked         TINYINT(1)   NOT NULL DEFAULT 0,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_auth_refresh_token_token_hash (token_hash),
    KEY idx_auth_refresh_token_user_id (user_id),
    KEY idx_auth_refresh_token_expires_at (expires_at),
    KEY idx_auth_refresh_token_revoked (revoked)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
