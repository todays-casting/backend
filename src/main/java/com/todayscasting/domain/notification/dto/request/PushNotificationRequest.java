package com.todayscasting.domain.notification.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Map;

@Schema(description = "푸시 알림 발송 요청")
public record PushNotificationRequest(
        @NotBlank
        @Size(max = 100)
        @Schema(description = "푸시 알림 제목", example = "오늘의 캐스팅")
        String title,

        @NotBlank
        @Size(max = 300)
        @Schema(description = "푸시 알림 본문", example = "오늘의 캐스팅 카드가 도착했어요.")
        String body,

        @Schema(description = "앱에서 화면 이동 등에 사용할 추가 데이터", example = "{\"type\":\"TEST\"}")
        Map<String, String> data
) {
}
