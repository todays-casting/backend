CREATE TABLE user_fcm_tokens (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 user_id BIGINT NOT NULL,
                                 token VARCHAR(512) NOT NULL,

                                 created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                 deleted_at DATETIME NULL,

                                 UNIQUE KEY uk_user_fcm_tokens_token (token),
                                 KEY idx_user_fcm_tokens_user_id (user_id),
                                 KEY idx_user_fcm_tokens_deleted_at (deleted_at),

                                 CONSTRAINT fk_user_fcm_tokens_user
                                     FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
