CREATE TABLE notifications (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               user_id BIGINT NOT NULL,
                               type VARCHAR(50) NOT NULL,
                               title VARCHAR(100) NOT NULL,
                               body VARCHAR(300) NOT NULL,
                               data JSON,
                               is_read BOOLEAN NOT NULL DEFAULT FALSE,
                               read_at DATETIME NULL,

                               created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                               deleted_at DATETIME NULL,

                               KEY idx_notifications_user_created (user_id, created_at),
                               KEY idx_notifications_user_read (user_id, is_read),
                               KEY idx_notifications_deleted_at (deleted_at),

                               CONSTRAINT fk_notifications_user
                                   FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
