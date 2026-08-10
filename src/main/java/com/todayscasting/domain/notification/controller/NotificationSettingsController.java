package com.todayscasting.domain.notification.controller;

import com.todayscasting.common.response.ApiResponse;
import com.todayscasting.domain.notification.dto.request.NotificationSettingsUpdateRequest;
import com.todayscasting.domain.notification.dto.response.NotificationSettingsResponse;
import com.todayscasting.domain.notification.service.NotificationSettingsService;
import com.todayscasting.domain.record.support.AuthenticatedUserResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users/me/notification-settings")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "FCM 토큰 저장 및 푸시 알림 API")
public class NotificationSettingsController {

    private final NotificationSettingsService notificationSettingsService;
    private final AuthenticatedUserResolver authenticatedUserResolver;

    @GetMapping
    @Operation(
            summary = "내 알림 설정 조회",
            description = "로그인한 사용자의 푸시 알림 사용 여부와 오늘 기록 리마인더 시간을 조회합니다."
    )
    public ApiResponse<NotificationSettingsResponse> getSettings(
            @AuthenticationPrincipal String email
    ) {
        Long userId = authenticatedUserResolver.resolveUserId(email);
        return ApiResponse.onSuccess(notificationSettingsService.getSettings(userId));
    }

    @PutMapping
    @Operation(
            summary = "내 알림 설정 변경",
            description = "푸시 알림 사용 여부와 오늘 기록 리마인더 발송 시간을 변경합니다. 리마인더를 켤 때는 dailyReminderTime이 필요합니다."
    )
    public ApiResponse<NotificationSettingsResponse> updateSettings(
            @Valid @RequestBody NotificationSettingsUpdateRequest request,
            @AuthenticationPrincipal String email
    ) {
        Long userId = authenticatedUserResolver.resolveUserId(email);
        return ApiResponse.onSuccess(notificationSettingsService.updateSettings(userId, request));
    }
}
