package com.todayscasting.domain.notification.repository;

import com.todayscasting.domain.notification.entity.UserSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface UserSettingsRepository extends JpaRepository<UserSettings, Long> {

    Optional<UserSettings> findByUserIdAndDeletedAtIsNull(Long userId);

    List<UserSettings> findByPushEnabledTrueAndDailyReminderEnabledTrueAndDailyReminderTimeAndDeletedAtIsNull(
            LocalTime dailyReminderTime
    );
}
