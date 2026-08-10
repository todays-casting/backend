package com.todayscasting.domain.notification.controller;

import com.todayscasting.common.response.ApiResponse;
import com.todayscasting.domain.notification.dto.request.FcmTokenSaveRequest;
import com.todayscasting.domain.notification.service.FcmTokenService;
import com.todayscasting.domain.record.support.AuthenticatedUserResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users/me/fcm-token")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "FCM 토큰 저장 및 푸시 알림 API")
public class FcmTokenController {

    private final FcmTokenService fcmTokenService;
    private final AuthenticatedUserResolver authenticatedUserResolver;

    @PostMapping
    @Operation(
            summary = "내 FCM 토큰 저장",
            description = "로그인한 사용자의 Android FCM device token을 저장합니다. 앱 실행 또는 로그인 후 발급된 토큰을 이 API로 전송하면 이후 푸시 알림 발송에 사용됩니다."
    )
    public ApiResponse<Void> save(
            @Valid @RequestBody FcmTokenSaveRequest request,
            @AuthenticationPrincipal String email
    ) {
        Long userId = authenticatedUserResolver.resolveUserId(email);
        fcmTokenService.saveToken(userId, request);
        return ApiResponse.onSuccess();
    }
}
