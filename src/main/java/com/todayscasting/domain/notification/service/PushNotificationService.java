package com.todayscasting.domain.notification.service;

import com.todayscasting.domain.notification.dto.request.PushNotificationRequest;
import com.todayscasting.domain.notification.dto.response.PushNotificationResponse;

public interface PushNotificationService {

    PushNotificationResponse sendToUser(Long userId, PushNotificationRequest request);

    PushNotificationResponse sendCastingCardReady(Long userId, Long dailyRecordId);

    PushNotificationResponse sendDailyRecordReminder(Long userId);

    PushNotificationResponse sendDraftRecordReminder(Long userId, Long dailyRecordId);
}
