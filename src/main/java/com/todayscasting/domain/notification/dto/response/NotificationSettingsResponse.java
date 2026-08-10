package com.todayscasting.domain.notification.dto.response;

import com.todayscasting.domain.notification.entity.UserSettings;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalTime;

@Schema(description = "알림 설정 응답")
public record NotificationSettingsResponse(
        @Schema(description = "푸시 알림 전체 사용 여부", example = "true")
        boolean pushEnabled,

        @Schema(description = "오늘 기록 리마인더 사용 여부", example = "true")
        boolean dailyReminderEnabled,

        @Schema(description = "오늘 기록 리마인더 발송 시간", example = "21:00")
        LocalTime dailyReminderTime
) {

    public static NotificationSettingsResponse from(UserSettings settings) {
        return new NotificationSettingsResponse(
                settings.isPushEnabled(),
                settings.isDailyReminderEnabled(),
                settings.getDailyReminderTime()
        );
    }
}
