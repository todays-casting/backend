package com.todayscasting.domain.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.todayscasting.common.code.status.ErrorStatus;
import com.todayscasting.common.exception.GeneralException;
import com.todayscasting.domain.notification.dto.request.PushNotificationRequest;
import com.todayscasting.domain.notification.dto.response.PushNotificationResponse;
import com.todayscasting.domain.notification.entity.Notification;
import com.todayscasting.domain.notification.entity.NotificationType;
import com.todayscasting.domain.notification.entity.UserFcmToken;
import com.todayscasting.domain.notification.repository.NotificationRepository;
import com.todayscasting.domain.notification.repository.UserFcmTokenRepository;
import com.todayscasting.domain.notification.repository.UserSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PushNotificationServiceImpl implements PushNotificationService {

    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder().build();

    private final UserFcmTokenRepository userFcmTokenRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final NotificationRepository notificationRepository;
    private final ObjectProvider<FirebaseMessaging> firebaseMessagingProvider;
    private final PlatformTransactionManager transactionManager;

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

        PushNotificationResponse response = send(userId, request, false);
        if (response.successCount() > 0) {
            saveNotification(userId, type, request);
        }
        return response;
    }

    @Override
    public PushNotificationResponse sendDailyRecordReminder(Long userId) {
        NotificationType type = NotificationType.DAILY_RECORD_REMINDER;
        PushNotificationRequest request = new PushNotificationRequest(
                type.title(),
                type.body(),
                Map.of("type", type.name())
        );

        PushNotificationResponse response = send(userId, request, false);
        if (response.successCount() > 0) {
            saveNotification(userId, type, request);
        }
        return response;
    }

    @Override
    public PushNotificationResponse sendDraftRecordReminder(Long userId, Long dailyRecordId) {
        NotificationType type = NotificationType.DRAFT_RECORD_REMINDER;
        PushNotificationRequest request = new PushNotificationRequest(
                type.title(),
                type.body(),
                Map.of(
                        "type", type.name(),
                        "dailyRecordId", String.valueOf(dailyRecordId)
                )
        );

        PushNotificationResponse response = send(userId, request, false);
        if (response.successCount() > 0) {
            saveNotification(userId, type, request);
        }
        return response;
    }

    private void saveNotification(Long userId, NotificationType type, PushNotificationRequest request) {
        notificationRepository.save(Notification.create(
                userId,
                type,
                request.title(),
                request.body(),
                toJson(request.data())
        ));
    }

    private PushNotificationResponse send(Long userId, PushNotificationRequest request, boolean failWhenUnavailable) {
        NotificationTargets targets = loadTargets(userId);
        if (!targets.pushEnabled()) {
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

        for (String token : targets.tokens()) {
            try {
                firebaseMessaging.send(toMessage(token, request));
                successCount++;
            } catch (FirebaseMessagingException e) {
                failureCount++;
            }
        }

        return new PushNotificationResponse(successCount, failureCount);
    }

    private NotificationTargets loadTargets(Long userId) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setReadOnly(true);
        return transactionTemplate.execute(status -> {
            boolean pushEnabled = userSettingsRepository.findByUserIdAndDeletedAtIsNull(userId)
                    .map(settings -> settings.isPushEnabled())
                    .orElse(true);
            if (!pushEnabled) {
                return new NotificationTargets(false, List.of());
            }

            List<String> tokens = userFcmTokenRepository.findByUserIdAndDeletedAtIsNull(userId).stream()
                    .map(UserFcmToken::getToken)
                    .toList();
            return new NotificationTargets(true, tokens);
        });
    }

    private record NotificationTargets(boolean pushEnabled, List<String> tokens) {
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

    private String toJson(Map<String, String> data) {
        if (data == null || data.isEmpty()) {
            return null;
        }

        try {
            return OBJECT_MAPPER.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new GeneralException(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
