package com.todayscasting.domain.mypage.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalTime;

@Schema(description = "마이페이지 요약 응답")
public record MyPageResponse(
        @Schema(description = "사용자 닉네임", example = "서연")
        String nickname,

        @Schema(description = "전체 완료 기록 수", example = "27")
        long totalRecordCount,

        @Schema(description = "연속 기록 일수", example = "12")
        int continuousRecordDays,

        @Schema(description = "찜한 캐스팅 카드 수", example = "38")
        long favoriteCastingCardCount,

        @Schema(description = "가입 후 경과 일수", example = "25")
        long joinedDays,

        @Schema(description = "푸시 알림 사용 여부", example = "true")
        boolean pushEnabled,

        @Schema(description = "오늘 기록 리마인더 사용 여부", example = "true")
        boolean dailyReminderEnabled,

        @Schema(description = "오늘 기록 리마인더 발송 시간", example = "21:00")
        LocalTime dailyReminderTime
) {
}
