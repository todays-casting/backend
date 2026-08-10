package com.todayscasting.domain.record.service;

import com.todayscasting.common.exception.GeneralException;
import com.todayscasting.domain.casting.entity.CastingCard;
import com.todayscasting.domain.casting.repository.CastingCardRepository;
import com.todayscasting.domain.record.dto.response.HistoryCardResponse;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HistoryServiceImplTest {

    @Mock
    private DailyRecordRepository dailyRecordRepository;

    @Mock
    private CastingCardRepository castingCardRepository;

    @InjectMocks
    private HistoryServiceImpl historyService;

    // 캐스팅카드가 있는 record(10L)만 결과에 남고, 없는 record(20L)는 제외되는지 검증
    @Test
    void excludesRecordsWithoutCastingCard() {
        LocalDate start = LocalDate.of(2025, 5, 1);
        LocalDate end = LocalDate.of(2025, 5, 7);

        DailyRecord recordWithCard = DailyRecord.create(1L, LocalDate.of(2025, 5, 3), "내용1", List.of("GOOD"), List.of(), List.of(), DailyRecord.Status.COMPLETED);
        ReflectionTestUtils.setField(recordWithCard, "id", 10L);

        DailyRecord recordWithoutCard = DailyRecord.create(1L, LocalDate.of(2025, 5, 5), "내용2", List.of("SAD"), List.of(), List.of(), DailyRecord.Status.COMPLETED);

        ReflectionTestUtils.setField(recordWithoutCard, "id", 20L);

        when(dailyRecordRepository.findByUserIdAndRecordDateBetweenAndStatusAndDeletedAtIsNullOrderByRecordDateAsc(
                1L, start, end, DailyRecord.Status.COMPLETED))
                .thenReturn(List.of(recordWithCard, recordWithoutCard));

        CastingCard card = CastingCard.builder()
                .dailyRecordId(10L)
                .title("따뜻한 조연")
                .genre("드라마")
                .roleName("옆집 이웃")
                .highlight("하이라이트")
                .oneLineComment("코멘트")
                .build();

        when(castingCardRepository.findByDailyRecordIdIn(List.of(10L, 20L)))
                .thenReturn(List.of(card));

        List<HistoryCardResponse> result = historyService.getHistory(1L, start, end);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).recordId()).isEqualTo(10L);
        assertThat(result.get(0).title()).isEqualTo("따뜻한 조연");
        assertThat(result.get(0).isFavorite()).isFalse();
    }

    // 날짜범위에 기록이 없으면 빈 리스트 반환 + 캐스팅카드 조회는 아예 안 하는지(불필요한 쿼리 방지) 검증
    @Test
    void returnsEmptyListAndSkipsCastingQueryWhenNoRecordsInRange() {
        LocalDate start = LocalDate.of(2025, 6, 1);
        LocalDate end = LocalDate.of(2025, 6, 7);

        when(dailyRecordRepository.findByUserIdAndRecordDateBetweenAndStatusAndDeletedAtIsNullOrderByRecordDateAsc(
                1L, start, end, DailyRecord.Status.COMPLETED))
                .thenReturn(List.of());

        List<HistoryCardResponse> result = historyService.getHistory(1L, start, end);

        assertThat(result).isEmpty();
        verify(castingCardRepository, never()).findByDailyRecordIdIn(anyList());
    }

    // startDate가 endDate보다 늦으면 예외 던지고, Repository 조회 자체를 안 하는지 검증
    @Test
    void throwsExceptionWhenStartDateAfterEndDate() {
        LocalDate start = LocalDate.of(2025, 5, 18);
        LocalDate end = LocalDate.of(2025, 5, 12);

        assertThatThrownBy(() -> historyService.getHistory(1L, start, end))
                .isInstanceOf(GeneralException.class);

        verifyNoInteractions(dailyRecordRepository, castingCardRepository);
    }
}