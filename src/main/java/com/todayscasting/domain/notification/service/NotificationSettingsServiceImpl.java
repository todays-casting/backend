package com.todayscasting.domain.notification.service;

import com.todayscasting.common.code.status.ErrorStatus;
import com.todayscasting.common.exception.GeneralException;
import com.todayscasting.domain.notification.dto.request.NotificationSettingsUpdateRequest;
import com.todayscasting.domain.notification.dto.response.NotificationSettingsResponse;
import com.todayscasting.domain.notification.entity.UserSettings;
import com.todayscasting.domain.notification.repository.UserSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationSettingsServiceImpl implements NotificationSettingsService {

    private final UserSettingsRepository userSettingsRepository;

    @Override
    @Transactional
    public NotificationSettingsResponse getSettings(Long userId) {
        UserSettings settings = getOrCreate(userId);
        return NotificationSettingsResponse.from(settings);
    }

    @Override
    @Transactional
    public NotificationSettingsResponse updateSettings(Long userId, NotificationSettingsUpdateRequest request) {
        if (request.dailyReminderEnabled() && request.dailyReminderTime() == null) {
            throw new GeneralException(ErrorStatus.MISSING_PARAMETER);
        }

        UserSettings settings = getOrCreate(userId);
        settings.update(request.pushEnabled(), request.dailyReminderEnabled(), request.dailyReminderTime());
        return NotificationSettingsResponse.from(settings);
    }

    private UserSettings getOrCreate(Long userId) {
        return userSettingsRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseGet(() -> userSettingsRepository.save(UserSettings.createDefault(userId)));
    }
}
