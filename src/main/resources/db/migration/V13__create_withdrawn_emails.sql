CREATE TABLE withdrawn_emails (
                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                  email_hash CHAR(64) NOT NULL,
                                  withdrawn_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                  UNIQUE KEY uk_withdrawn_emails_email_hash (email_hash)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
