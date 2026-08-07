package com.todayscasting.domain.notification.controller;

import com.todayscasting.common.response.ApiResponse;
import com.todayscasting.domain.notification.dto.request.PushNotificationRequest;
import com.todayscasting.domain.notification.dto.response.PushNotificationResponse;
import com.todayscasting.domain.notification.service.PushNotificationService;
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
@RequestMapping("/users/me/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "FCM 토큰 저장 및 푸시 알림 API")
public class PushNotificationController {

    private final PushNotificationService pushNotificationService;
    private final AuthenticatedUserResolver authenticatedUserResolver;

    @PostMapping("/test")
    @Operation(
            summary = "내 기기로 테스트 푸시 발송",
            description = "로그인한 사용자에게 저장된 FCM 토큰으로 테스트 푸시 알림을 발송합니다. Firebase Admin SDK 설정과 토큰 저장 여부를 확인할 때 사용합니다."
    )
    public ApiResponse<PushNotificationResponse> sendTest(
            @Valid @RequestBody PushNotificationRequest request,
            @AuthenticationPrincipal String email
    ) {
        Long userId = authenticatedUserResolver.resolveUserId(email);
        PushNotificationResponse response = pushNotificationService.sendToUser(userId, request);
        return ApiResponse.onSuccess(response);
    }
}
