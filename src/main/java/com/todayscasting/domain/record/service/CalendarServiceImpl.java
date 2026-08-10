package com.todayscasting.domain.record.service;

import com.todayscasting.domain.casting.entity.CastingCard;
import com.todayscasting.domain.casting.repository.CastingCardRepository;
import com.todayscasting.domain.record.converter.DailyRecordConverter;
import com.todayscasting.domain.record.dto.response.CalendarResponse;
import com.todayscasting.domain.record.entity.DailyRecord;
import com.todayscasting.domain.record.repository.DailyRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalendarServiceImpl implements CalendarService {

    private final DailyRecordRepository dailyRecordRepository;
    private final CastingCardRepository castingCardRepository;

    @Override
    public List<CalendarResponse> getMonthlyCalendar(Long userId, YearMonth yearMonth) {
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<DailyRecord> records = dailyRecordRepository
                .findByUserIdAndRecordDateBetweenAndStatusAndDeletedAtIsNullOrderByRecordDateAsc(
                        userId, startDate, endDate, DailyRecord.Status.COMPLETED);

        if (records.isEmpty()) {
            return List.of();
        }
        List<Long> recordIds = records.stream()
                .map(DailyRecord::getId)
                .toList();

        Set<Long> favoriteRecordIds = castingCardRepository
                .findByDailyRecordIdInAndIsFavoriteTrue(recordIds)
                .stream()
                .map(CastingCard::getDailyRecordId)
                .collect(Collectors.toSet());

        return records.stream()
                .map(record -> DailyRecordConverter.toCalendarResponse(record, favoriteRecordIds.contains(record.getId())))
                .toList();
    }
}
