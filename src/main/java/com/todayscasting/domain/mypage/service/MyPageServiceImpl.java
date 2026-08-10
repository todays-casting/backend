package com.todayscasting.domain.mypage.service;

import com.todayscasting.common.exception.GeneralException;
import com.todayscasting.domain.auth.code.AuthErrorStatus;
import com.todayscasting.domain.casting.repository.CastingCardRepository;
import com.todayscasting.domain.mypage.dto.response.MyPageResponse;
import com.todayscasting.domain.notification.entity.UserSettings;
import com.todayscasting.domain.notification.repository.UserSettingsRepository;
import com.todayscasting.domain.record.entity.DailyRecord;
import com.todayscasting.domain.record.repository.DailyRecordRepository;
import com.todayscasting.domain.user.entity.User;
import com.todayscasting.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageServiceImpl implements MyPageService {

    private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");

    private final UserRepository userRepository;
    private final DailyRecordRepository dailyRecordRepository;
    private final CastingCardRepository castingCardRepository;
    private final UserSettingsRepository userSettingsRepository;

    @Override
    public MyPageResponse getMyPage(Long userId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new GeneralException(AuthErrorStatus.USER_NOT_FOUND));
        UserSettings settings = userSettingsRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElse(null);

        long totalRecordCount = dailyRecordRepository.countByUserIdAndStatusAndDeletedAtIsNull(
                userId,
                DailyRecord.Status.COMPLETED
        );
        List<LocalDate> completedRecordDates = dailyRecordRepository
                .findRecordDatesByUserIdAndStatusOrderByRecordDateDesc(userId, DailyRecord.Status.COMPLETED);

        return new MyPageResponse(
                user.getNickname(),
                totalRecordCount,
                calculateContinuousRecordDays(completedRecordDates),
                castingCardRepository.countFavoritesByUserId(userId),
                calculateJoinedDays(user),
                settings == null || settings.isPushEnabled(),
                settings != null && settings.isDailyReminderEnabled(),
                settings != null ? settings.getDailyReminderTime() : null
        );
    }

    private int calculateContinuousRecordDays(List<LocalDate> recordDates) {
        Set<LocalDate> dateSet = new HashSet<>(recordDates);
        LocalDate today = LocalDate.now(SERVICE_ZONE);
        LocalDate cursor = dateSet.contains(today) ? today : today.minusDays(1);

        int days = 0;
        while (dateSet.contains(cursor)) {
            days++;
            cursor = cursor.minusDays(1);
        }
        return days;
    }

    private long calculateJoinedDays(User user) {
        LocalDate joinedDate = user.getCreatedAt().atZone(SERVICE_ZONE).toLocalDate();
        LocalDate today = LocalDate.now(SERVICE_ZONE);
        return ChronoUnit.DAYS.between(joinedDate, today) + 1;
    }
}
