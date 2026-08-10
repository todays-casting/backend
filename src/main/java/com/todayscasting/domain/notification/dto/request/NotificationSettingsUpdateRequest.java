package com.todayscasting.domain.notification.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalTime;

@Schema(description = "알림 설정 변경 요청")
public record NotificationSettingsUpdateRequest(
        @Schema(description = "푸시 알림 전체 사용 여부", example = "true")
        boolean pushEnabled,

        @Schema(description = "오늘 기록 리마인더 사용 여부", example = "true")
        boolean dailyReminderEnabled,

        @Schema(description = "오늘 기록 리마인더 발송 시간", example = "21:00")
        LocalTime dailyReminderTime
) {
}
