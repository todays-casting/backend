package com.todayscasting.domain.notification.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "FCM 토큰 저장 요청")
public record FcmTokenSaveRequest(
        @NotBlank
        @Size(max = 512)
        @Schema(
                description = "Android 앱에서 발급받은 FCM device token",
                example = "dqymoWgnQeKk0v..."
        )
        String token
) {
}
