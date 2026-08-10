package com.todayscasting.domain.notification.repository;

import com.todayscasting.domain.notification.entity.UserFcmToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserFcmTokenRepository extends JpaRepository<UserFcmToken, Long> {

    Optional<UserFcmToken> findByToken(String token);

    List<UserFcmToken> findByUserIdAndDeletedAtIsNull(Long userId);

    @Modifying
    @Query(value = """
            INSERT INTO user_fcm_tokens (user_id, token)
            VALUES (:userId, :token)
            ON DUPLICATE KEY UPDATE
                user_id = VALUES(user_id),
                deleted_at = NULL,
                updated_at = CURRENT_TIMESTAMP
            """, nativeQuery = true)
    void upsertToken(@Param("userId") Long userId, @Param("token") String token);
}
