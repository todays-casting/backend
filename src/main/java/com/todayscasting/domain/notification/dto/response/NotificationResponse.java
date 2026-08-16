package com.todayscasting.domain.notification.dto.response;

import com.todayscasting.domain.notification.entity.Notification;
import com.todayscasting.domain.notification.entity.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "알림 목록 항목")
public record NotificationResponse(
        @Schema(description = "알림 ID", example = "1")
        Long id,

        @Schema(description = "알림 타입", example = "CASTING_CARD_READY")
        NotificationType type,

        @Schema(description = "알림 제목", example = "오늘의 캐스팅")
        String title,

        @Schema(description = "알림 본문", example = "오늘의 캐스팅 카드가 도착했어요.")
        String body,

        @Schema(description = "화면 이동 등에 사용하는 추가 데이터", example = "{\"dailyRecordId\":\"10\"}")
        Map<String, String> data,

        @Schema(description = "읽음 여부", example = "false")
        boolean read,

        @Schema(description = "생성 시각")
        LocalDateTime createdAt
) {

    public static NotificationResponse from(Notification notification, Map<String, String> data) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getBody(),
                data,
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
