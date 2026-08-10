package com.todayscasting.domain.notification.repository;

import com.todayscasting.domain.notification.entity.UserSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface UserSettingsRepository extends JpaRepository<UserSettings, Long> {

    Optional<UserSettings> findByUserIdAndDeletedAtIsNull(Long userId);

    List<UserSettings> findByPushEnabledTrueAndDailyReminderEnabledTrueAndDailyReminderTimeAndDeletedAtIsNull(
            LocalTime dailyReminderTime
    );

    @Modifying
    @Query(value = """
            INSERT INTO user_settings (user_id)
            VALUES (:userId)
            ON DUPLICATE KEY UPDATE
                deleted_at = NULL,
                updated_at = CURRENT_TIMESTAMP
            """, nativeQuery = true)
    void ensureDefaultSettings(@Param("userId") Long userId);
}
