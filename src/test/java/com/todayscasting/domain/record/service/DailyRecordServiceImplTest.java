package com.todayscasting.domain.record.service;

import com.todayscasting.common.code.status.ErrorStatus;
import com.todayscasting.common.exception.GeneralException;
import com.todayscasting.domain.record.converter.DailyRecordConverter;
import com.todayscasting.domain.record.dto.request.DailyRecordCreateRequest;
import com.todayscasting.domain.record.dto.request.DailyRecordUpdateRequest;
import com.todayscasting.domain.record.dto.response.DailyRecordResponse;
import com.todayscasting.domain.record.entity.DailyRecord;
import com.todayscasting.domain.record.repository.DailyRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DailyRecordServiceImplTest {

    @Mock
    private DailyRecordRepository dailyRecordRepository;

    @InjectMocks
    private DailyRecordServiceImpl dailyRecordService;

    @Test
    void createsDailyRecord() {
        DailyRecordCreateRequest request = new DailyRecordCreateRequest(
                LocalDate.of(2026, 7, 9), "오늘 발표 준비 완료", List.of("GOOD"),
                List.of("뿌듯함"), List.of("개발")
        );
        DailyRecord saved = DailyRecordConverter.toEntity(1L, request);
        when(dailyRecordRepository.save(any(DailyRecord.class))).thenReturn(saved);

        DailyRecordResponse response = dailyRecordService.create(1L, request);

        assertThat(response.content()).isEqualTo("오늘 발표 준비 완료");
        assertThat(response.mood()).isEqualTo(List.of("GOOD"));
    }

    @Test
    void throwsNotFoundWhenUpdatingMissingRecord() {
        when(dailyRecordRepository.findByIdAndUserIdAndDeletedAtIsNull(999L, 1L)).thenReturn(Optional.empty());

        DailyRecordUpdateRequest request = new DailyRecordUpdateRequest("내용", List.of("GOOD"), List.of(), List.of());

        assertThatThrownBy(() -> dailyRecordService.update(1L, 999L, request))
                .isInstanceOf(GeneralException.class);
    }

    @Test
    void returnsNullWhenDateHasNoRecord() {
        when(dailyRecordRepository.findByUserIdAndRecordDateAndDeletedAtIsNull(1L, LocalDate.of(2026, 7, 9)))
                .thenReturn(Optional.empty());

        DailyRecordResponse response = dailyRecordService.getByDate(1L, LocalDate.of(2026, 7, 9));

        assertThat(response).isNull();
    }

    @Test
    void deletesDailyRecordAsSoftDelete() {
        DailyRecord record = DailyRecord.create(1L, LocalDate.of(2026, 7, 9), "내용", List.of("GOOD"), List.of(), List.of());
        when(dailyRecordRepository.findByIdAndUserIdAndDeletedAtIsNull(1L, 1L)).thenReturn(Optional.of(record));

        dailyRecordService.delete(1L, 1L);

        assertThat(record.isDeleted()).isTrue();
    }

    @Test
    void updatesDailyRecord() {
        DailyRecord record = DailyRecord.create(1L, LocalDate.of(2026, 7, 9), "원래 내용", List.of("GOOD"), List.of(), List.of());
        when(dailyRecordRepository.findByIdAndUserIdAndDeletedAtIsNull(1L, 1L)).thenReturn(Optional.of(record));

        DailyRecordUpdateRequest request = new DailyRecordUpdateRequest("수정된 내용", List.of("BAD"), List.of("피곤함"), List.of());

        DailyRecordResponse response = dailyRecordService.update(1L, 1L, request);

        assertThat(response.content()).isEqualTo("수정된 내용");
        assertThat(response.mood()).isEqualTo(List.of("BAD"));
    }

    @Test
    void returnsRecordWhenDateHasRecord() {
        DailyRecord record = DailyRecord.create(1L, LocalDate.of(2026, 7, 9), "오늘 기록", List.of("GOOD"), List.of(), List.of());
        when(dailyRecordRepository.findByUserIdAndRecordDateAndDeletedAtIsNull(1L, LocalDate.of(2026, 7, 9)))
                .thenReturn(Optional.of(record));

        DailyRecordResponse response = dailyRecordService.getByDate(1L, LocalDate.of(2026, 7, 9));

        assertThat(response.content()).isEqualTo("오늘 기록");
    }

    @Test
    void throwsDuplicateResourceOnConcurrentCreate() {
        DailyRecordCreateRequest request = new DailyRecordCreateRequest(
                LocalDate.of(2026, 7, 9), "동시 작성 시도", List.of("GOOD"), List.of(), List.of()
        );
        when(dailyRecordRepository.findByUserIdAndRecordDate(1L, LocalDate.of(2026, 7, 9)))
                .thenReturn(Optional.empty());
        when(dailyRecordRepository.save(any(DailyRecord.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> dailyRecordService.create(1L, request))
                .isInstanceOf(GeneralException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorStatus.DUPLICATE_RESOURCE);
    }

    @Test
    // 리스트를 제대로 반환하는지 테스트
    void returnsRecordsWhenTagMatches() {
        DailyRecord record = DailyRecord.create(1L, LocalDate.of(2026, 7, 9), "오늘 기록", List.of("GOOD"), List.of("따뜻해요"), List.of("로맨스"));
        when(dailyRecordRepository.findByTags(1L, null, "따뜻해요", null))
                .thenReturn(List.of(record));

        List<DailyRecordResponse> response = dailyRecordService.getByTags(1L, null, "따뜻해요", null);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).moodTags()).isEqualTo(List.of("따뜻해요"));
    }

    @Test
    // 셋다 null로 호출했을때 진짜 400_3 오류가 발생하는지 테스트
    void throwsMissingParameterWhenNoTagGiven() {
        assertThatThrownBy(() -> dailyRecordService.getByTags(1L, null, null, null))
                .isInstanceOf(GeneralException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorStatus.MISSING_PARAMETER);
    }

    @Test
    void returnsRecordWhenIdExists() {
        DailyRecord record = DailyRecord.create(1L, LocalDate.of(2026, 7, 9), "오늘 기록", List.of("GOOD"), List.of(), List.of());
        when(dailyRecordRepository.findByIdAndUserIdAndDeletedAtIsNull(1L, 1L)).thenReturn(Optional.of(record));

        DailyRecordResponse response = dailyRecordService.getById(1L, 1L);

        assertThat(response.content()).isEqualTo("오늘 기록");
    }

    @Test
    void throwsNotFoundWhenIdDoesNotExist() {
        when(dailyRecordRepository.findByIdAndUserIdAndDeletedAtIsNull(999L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dailyRecordService.getById(1L, 999L))
                .isInstanceOf(GeneralException.class);
    }
}