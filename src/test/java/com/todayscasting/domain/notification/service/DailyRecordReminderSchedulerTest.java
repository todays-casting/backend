package com.todayscasting.domain.notification.service;

import com.todayscasting.domain.notification.repository.UserSettingsRepository;
import com.todayscasting.domain.record.entity.DailyRecord;
import com.todayscasting.domain.record.repository.DailyRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DailyRecordReminderSchedulerTest {

    @Mock
    private UserSettingsRepository userSettingsRepository;

    @Mock
    private DailyRecordRepository dailyRecordRepository;

    @Mock
    private PushNotificationService pushNotificationService;

    @InjectMocks
    private DailyRecordReminderScheduler scheduler;

    @Test
    void sendsDailyRecordReminderWhenTodayRecordDoesNotExist() {
        LocalDate today = LocalDate.of(2026, 8, 20);
        when(dailyRecordRepository.findByUserIdAndRecordDateAndDeletedAtIsNull(1L, today))
                .thenReturn(Optional.empty());

        ReflectionTestUtils.invokeMethod(scheduler, "sendReminderIfNoRecord", 1L, today);

        verify(pushNotificationService).sendDailyRecordReminder(1L);
        verify(pushNotificationService, never()).sendDraftRecordReminder(1L, 10L);
    }

    @Test
    void sendsDraftRecordReminderWhenTodayRecordIsDraft() {
        LocalDate today = LocalDate.of(2026, 8, 20);
        DailyRecord record = DailyRecord.create(
                1L,
                today,
                "작성 중",
                List.of(),
                List.of(),
                List.of(),
                DailyRecord.Status.DRAFT
        );
        ReflectionTestUtils.setField(record, "id", 10L);
        when(dailyRecordRepository.findByUserIdAndRecordDateAndDeletedAtIsNull(1L, today))
                .thenReturn(Optional.of(record));

        ReflectionTestUtils.invokeMethod(scheduler, "sendReminderIfNoRecord", 1L, today);

        verify(pushNotificationService).sendDraftRecordReminder(1L, 10L);
        verify(pushNotificationService, never()).sendDailyRecordReminder(1L);
    }

    @Test
    void doesNotSendReminderWhenTodayRecordIsCompleted() {
        LocalDate today = LocalDate.of(2026, 8, 20);
        DailyRecord record = DailyRecord.create(
                1L,
                today,
                "완료",
                List.of(),
                List.of(),
                List.of(),
                DailyRecord.Status.COMPLETED
        );
        when(dailyRecordRepository.findByUserIdAndRecordDateAndDeletedAtIsNull(1L, today))
                .thenReturn(Optional.of(record));

        ReflectionTestUtils.invokeMethod(scheduler, "sendReminderIfNoRecord", 1L, today);

        verify(pushNotificationService, never()).sendDailyRecordReminder(1L);
        verify(pushNotificationService, never()).sendDraftRecordReminder(1L, 10L);
    }
}
