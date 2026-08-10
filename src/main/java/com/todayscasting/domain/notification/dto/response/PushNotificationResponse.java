package com.todayscasting.domain.notification.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "푸시 알림 발송 결과")
public record PushNotificationResponse(
        @Schema(description = "발송 성공 건수", example = "1")
        int successCount,

        @Schema(description = "발송 실패 건수", example = "0")
        int failureCount
) {
}
