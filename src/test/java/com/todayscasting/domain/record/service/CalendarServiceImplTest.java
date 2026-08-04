package com.todayscasting.domain.record.service;

import com.todayscasting.domain.casting.entity.CastingCard;
import com.todayscasting.domain.casting.repository.CastingCardRepository;
import com.todayscasting.domain.record.dto.response.CalendarResponse;
import com.todayscasting.domain.record.entity.DailyRecord;
import com.todayscasting.domain.record.repository.DailyRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalendarServiceImplTest {

    @Mock
    private DailyRecordRepository dailyRecordRepository;

    @Mock
    private CastingCardRepository castingCardRepository;

    @InjectMocks
    private CalendarServiceImpl calendarService;

    @Test
    void returnsMarkersWithFavoriteFlagForRecordsInMonth() {
        YearMonth yearMonth = YearMonth.of(2025, 5);

        DailyRecord record1 = DailyRecord.create(1L, LocalDate.of(2025, 5, 6), "내용1", List.of("GOOD"), List.of(), List.of());
        ReflectionTestUtils.setField(record1, "id", 10L);

        DailyRecord record2 = DailyRecord.create(1L, LocalDate.of(2025, 5, 12), "내용2", List.of("GOOD"), List.of(), List.of());
        ReflectionTestUtils.setField(record2, "id", 20L);

        when(dailyRecordRepository.findByUserIdAndRecordDateBetweenAndDeletedAtIsNullOrderByRecordDateAsc(
                1L, yearMonth.atDay(1), yearMonth.atEndOfMonth()))
                .thenReturn(List.of(record1, record2));

        CastingCard favoriteCard = CastingCard.builder().dailyRecordId(20L).build();
        favoriteCard.toggleFavorite(); // 생성 직후 기본값 false라서 true로 뒤집음

        when(castingCardRepository.findByDailyRecordIdInAndIsFavoriteTrue(List.of(10L, 20L)))
                .thenReturn(List.of(favoriteCard));

        List<CalendarResponse> result = calendarService.getMonthlyCalendar(1L, yearMonth);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).recordDate()).isEqualTo(LocalDate.of(2025, 5, 6));
        assertThat(result.get(0).isFavorite()).isFalse();
        assertThat(result.get(1).recordDate()).isEqualTo(LocalDate.of(2025, 5, 12));
        assertThat(result.get(1).isFavorite()).isTrue();
    }

    @Test
    void returnsEmptyListAndSkipsCastingQueryWhenNoRecordsInMonth() {
        YearMonth yearMonth = YearMonth.of(2025, 6);
        when(dailyRecordRepository.findByUserIdAndRecordDateBetweenAndDeletedAtIsNullOrderByRecordDateAsc(
                1L, yearMonth.atDay(1), yearMonth.atEndOfMonth()))
                .thenReturn(List.of());

        List<CalendarResponse> result = calendarService.getMonthlyCalendar(1L, yearMonth);

        assertThat(result).isEmpty();
        verify(castingCardRepository, never()).findByDailyRecordIdInAndIsFavoriteTrue(anyList());
    }
}