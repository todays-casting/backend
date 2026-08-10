package com.todayscasting.domain.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.todayscasting.common.code.status.ErrorStatus;
import com.todayscasting.common.exception.GeneralException;
import com.todayscasting.domain.notification.dto.request.PushNotificationRequest;
import com.todayscasting.domain.notification.dto.response.PushNotificationResponse;
import com.todayscasting.domain.notification.entity.NotificationType;
import com.todayscasting.domain.notification.entity.UserFcmToken;
import com.todayscasting.domain.notification.repository.UserFcmTokenRepository;
import com.todayscasting.domain.notification.repository.UserSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PushNotificationServiceImpl implements PushNotificationService {

    private final UserFcmTokenRepository userFcmTokenRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final ObjectProvider<FirebaseMessaging> firebaseMessagingProvider;

    @Override
    public PushNotificationResponse sendToUser(Long userId, PushNotificationRequest request) {
        return send(userId, request, true);
    }

    @Override
    public PushNotificationResponse sendCastingCardReady(Long userId, Long dailyRecordId) {
        NotificationType type = NotificationType.CASTING_CARD_READY;
        PushNotificationRequest request = new PushNotificationRequest(
                type.title(),
                type.body(),
                Map.of(
                        "type", type.name(),
                        "dailyRecordId", String.valueOf(dailyRecordId)
                )
        );

        return send(userId, request, false);
    }

    @Override
    public PushNotificationResponse sendDailyRecordReminder(Long userId) {
        NotificationType type = NotificationType.DAILY_RECORD_REMINDER;
        PushNotificationRequest request = new PushNotificationRequest(
                type.title(),
                type.body(),
                Map.of("type", type.name())
        );

        return send(userId, request, false);
    }

    private PushNotificationResponse send(Long userId, PushNotificationRequest request, boolean failWhenUnavailable) {
        if (!isPushEnabled(userId)) {
            return new PushNotificationResponse(0, 0);
        }

        FirebaseMessaging firebaseMessaging = firebaseMessagingProvider.getIfAvailable();
        if (firebaseMessaging == null) {
            if (!failWhenUnavailable) {
                return new PushNotificationResponse(0, 0);
            }
            throw new GeneralException(ErrorStatus.INTERNAL_SERVER_ERROR);
        }

        int successCount = 0;
        int failureCount = 0;

        for (UserFcmToken userFcmToken : userFcmTokenRepository.findByUserIdAndDeletedAtIsNull(userId)) {
            try {
                firebaseMessaging.send(toMessage(userFcmToken.getToken(), request));
                successCount++;
            } catch (FirebaseMessagingException e) {
                failureCount++;
            }
        }

        return new PushNotificationResponse(successCount, failureCount);
    }

    private boolean isPushEnabled(Long userId) {
        return userSettingsRepository.findByUserIdAndDeletedAtIsNull(userId)
                .map(settings -> settings.isPushEnabled())
                .orElse(true);
    }

    @SuppressWarnings("deprecation")
    private Message toMessage(String token, PushNotificationRequest request) {
        Message.Builder builder = Message.builder()
                .setToken(token)
                .setNotification(com.google.firebase.messaging.Notification.builder()
                        .setTitle(request.title())
                        .setBody(request.body())
                        .build());

        Map<String, String> data = request.data();
        if (data != null && !data.isEmpty()) {
            builder.putAllData(data);
        }

        return builder.build();
    }
}
