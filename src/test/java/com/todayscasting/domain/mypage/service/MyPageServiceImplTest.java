package com.todayscasting.domain.mypage.service;

import com.todayscasting.domain.casting.repository.CastingCardRepository;
import com.todayscasting.domain.mypage.dto.response.MyPageResponse;
import com.todayscasting.domain.notification.entity.UserSettings;
import com.todayscasting.domain.notification.repository.UserSettingsRepository;
import com.todayscasting.domain.record.entity.DailyRecord;
import com.todayscasting.domain.record.repository.DailyRecordRepository;
import com.todayscasting.domain.user.entity.User;
import com.todayscasting.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MyPageServiceImplTest {

    private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");

    @Mock
    private UserRepository userRepository;

    @Mock
    private DailyRecordRepository dailyRecordRepository;

    @Mock
    private CastingCardRepository castingCardRepository;

    @Mock
    private UserSettingsRepository userSettingsRepository;

    private MyPageServiceImpl myPageService;

    @BeforeEach
    void setUp() {
        myPageService = new MyPageServiceImpl(
                userRepository,
                dailyRecordRepository,
                castingCardRepository,
                userSettingsRepository
        );
    }

    @Test
    void returnsMyPageSummaryWithYesterdayBasedStreakWhenTodayRecordIsMissing() {
        LocalDate today = LocalDate.now(SERVICE_ZONE);
        User user = mock(User.class);
        UserSettings settings = UserSettings.createDefault(1L);
        settings.update(true, true, LocalTime.of(21, 0));

        when(user.getNickname()).thenReturn("서연");
        when(user.getCreatedAt()).thenReturn(LocalDateTime.now(SERVICE_ZONE).minusDays(24));
        when(userRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(user));
        when(userSettingsRepository.findByUserIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(settings));
        when(dailyRecordRepository.countByUserIdAndStatusAndDeletedAtIsNull(1L, DailyRecord.Status.COMPLETED))
                .thenReturn(27L);
        when(dailyRecordRepository.findRecordDatesByUserIdAndStatusOrderByRecordDateDesc(1L, DailyRecord.Status.COMPLETED))
                .thenReturn(List.of(today.minusDays(1), today.minusDays(2), today.minusDays(3)));
        when(castingCardRepository.countFavoritesByUserId(1L)).thenReturn(38L);

        MyPageResponse response = myPageService.getMyPage(1L);

        assertThat(response.nickname()).isEqualTo("서연");
        assertThat(response.totalRecordCount()).isEqualTo(27);
        assertThat(response.continuousRecordDays()).isEqualTo(3);
        assertThat(response.favoriteCastingCardCount()).isEqualTo(38);
        assertThat(response.joinedDays()).isEqualTo(25);
        assertThat(response.pushEnabled()).isTrue();
        assertThat(response.dailyReminderEnabled()).isTrue();
        assertThat(response.dailyReminderTime()).isEqualTo(LocalTime.of(21, 0));
    }

    @Test
    void returnsDefaultNotificationSettingsWhenSettingsDoNotExist() {
        LocalDate today = LocalDate.now(SERVICE_ZONE);
        User user = mock(User.class);

        when(user.getNickname()).thenReturn("서연");
        when(user.getCreatedAt()).thenReturn(LocalDateTime.now(SERVICE_ZONE));
        when(userRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(user));
        when(userSettingsRepository.findByUserIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());
        when(dailyRecordRepository.countByUserIdAndStatusAndDeletedAtIsNull(1L, DailyRecord.Status.COMPLETED))
                .thenReturn(1L);
        when(dailyRecordRepository.findRecordDatesByUserIdAndStatusOrderByRecordDateDesc(1L, DailyRecord.Status.COMPLETED))
                .thenReturn(List.of(today));
        when(castingCardRepository.countFavoritesByUserId(1L)).thenReturn(0L);

        MyPageResponse response = myPageService.getMyPage(1L);

        assertThat(response.continuousRecordDays()).isEqualTo(1);
        assertThat(response.pushEnabled()).isTrue();
        assertThat(response.dailyReminderEnabled()).isFalse();
        assertThat(response.dailyReminderTime()).isNull();
    }
}
