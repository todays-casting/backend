package com.todayscasting.domain.notification.entity;

import com.todayscasting.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@Table(name = "user_settings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSettings extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "push_enabled", nullable = false)
    private boolean pushEnabled;

    @Column(name = "daily_reminder_enabled", nullable = false)
    private boolean dailyReminderEnabled;

    @Column(name = "daily_reminder_time")
    private LocalTime dailyReminderTime;

    public static UserSettings createDefault(Long userId) {
        UserSettings settings = new UserSettings();
        settings.userId = userId;
        settings.pushEnabled = true;
        settings.dailyReminderEnabled = false;
        return settings;
    }

    public void update(boolean pushEnabled, boolean dailyReminderEnabled, LocalTime dailyReminderTime) {
        this.pushEnabled = pushEnabled;
        this.dailyReminderEnabled = dailyReminderEnabled;
        this.dailyReminderTime = dailyReminderTime;
    }
}
