package com.todayscasting.domain.notification.service;

import com.todayscasting.domain.notification.dto.response.NotificationResponse;

import java.util.List;

public interface NotificationService {

    List<NotificationResponse> getNotifications(Long userId, int limit);

    NotificationResponse markAsRead(Long userId, Long notificationId);
}
