package com.todayscasting.domain.notification.service;

import com.todayscasting.domain.notification.dto.request.NotificationSettingsUpdateRequest;
import com.todayscasting.domain.notification.dto.response.NotificationSettingsResponse;

public interface NotificationSettingsService {

    NotificationSettingsResponse getSettings(Long userId);

    NotificationSettingsResponse updateSettings(Long userId, NotificationSettingsUpdateRequest request);
}
