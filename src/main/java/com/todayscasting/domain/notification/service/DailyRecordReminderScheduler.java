package com.todayscasting.domain.notification.service;

import com.todayscasting.domain.notification.entity.UserSettings;
import com.todayscasting.domain.notification.repository.UserSettingsRepository;
import com.todayscasting.domain.record.entity.DailyRecord;
import com.todayscasting.domain.record.repository.DailyRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
public class DailyRecordReminderScheduler {

    private static final ZoneId REMINDER_ZONE = ZoneId.of("Asia/Seoul");

    private final UserSettingsRepository userSettingsRepository;
    private final DailyRecordRepository dailyRecordRepository;
    private final PushNotificationService pushNotificationService;

    @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
    public void sendDailyRecordReminders() {
        LocalDate today = LocalDate.now(REMINDER_ZONE);
        LocalTime currentMinute = LocalTime.now(REMINDER_ZONE).withSecond(0).withNano(0);

        for (UserSettings settings : userSettingsRepository
                .findByPushEnabledTrueAndDailyReminderEnabledTrueAndDailyReminderTimeAndDeletedAtIsNull(currentMinute)) {
            sendReminderIfNoRecord(settings.getUserId(), today);
        }
    }

    private void sendReminderIfNoRecord(Long userId, LocalDate today) {
        dailyRecordRepository.findByUserIdAndRecordDateAndDeletedAtIsNull(userId, today)
                .ifPresentOrElse(
                        record -> sendReminderIfDraft(record, userId),
                        () -> pushNotificationService.sendDailyRecordReminder(userId)
                );
    }

    private void sendReminderIfDraft(DailyRecord record, Long userId) {
        if (record.getStatus() == DailyRecord.Status.DRAFT) {
            pushNotificationService.sendDraftRecordReminder(userId, record.getId());
        }
    }
}
