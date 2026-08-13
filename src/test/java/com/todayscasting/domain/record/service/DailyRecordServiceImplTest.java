package com.todayscasting.domain.record.service;

import com.todayscasting.common.code.status.ErrorStatus;
import com.todayscasting.common.exception.GeneralException;
import com.todayscasting.domain.analysis.entity.AiAnalysisLog;
import com.todayscasting.domain.analysis.repository.AiAnalysisLogRepository;
import com.todayscasting.domain.casting.entity.CastingCard;
import com.todayscasting.domain.casting.repository.CastingCardRepository;
import com.todayscasting.domain.record.converter.DailyRecordConverter;
import com.todayscasting.domain.record.dto.request.DailyRecordCreateRequest;
import com.todayscasting.domain.record.dto.request.DailyRecordUpdateRequest;
import com.todayscasting.domain.record.dto.response.DailyRecordResponse;
import com.todayscasting.domain.record.dto.response.TodayScreen;
import com.todayscasting.domain.record.dto.response.TodayStatusResponse;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DailyRecordServiceImplTest {

    @Mock
    private DailyRecordRepository dailyRecordRepository;

    @InjectMocks
    private DailyRecordServiceImpl dailyRecordService;

    @Mock
    private AiAnalysisLogRepository aiAnalysisLogRepository;

    @Mock
    private CastingCardRepository castingCardRepository;

    @Test
    void createsDailyRecord() {
        DailyRecordCreateRequest request = new DailyRecordCreateRequest(
                LocalDate.of(2026, 7, 9), "오늘 발표 준비 완료", List.of("GOOD"),
                List.of("뿌듯함"), List.of("개발"), DailyRecord.Status.COMPLETED
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

        DailyRecordUpdateRequest request = new DailyRecordUpdateRequest("내용", List.of("GOOD"), List.of(), List.of(), DailyRecord.Status.COMPLETED);

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
        DailyRecord record = DailyRecord.create(1L, LocalDate.of(2026, 7, 9), "내용", List.of("GOOD"), List.of(), List.of(), DailyRecord.Status.COMPLETED);
        when(dailyRecordRepository.findByIdAndUserIdAndDeletedAtIsNull(1L, 1L)).thenReturn(Optional.of(record));

        dailyRecordService.delete(1L, 1L);

        assertThat(record.isDeleted()).isTrue();
    }

    @Test
    void updatesDailyRecord() {
        DailyRecord record = DailyRecord.create(1L, LocalDate.of(2026, 7, 9), "원래 내용", List.of("GOOD"), List.of(), List.of(), DailyRecord.Status.COMPLETED);
        when(dailyRecordRepository.findByIdAndUserIdAndDeletedAtIsNull(1L, 1L)).thenReturn(Optional.of(record));

        DailyRecordUpdateRequest request = new DailyRecordUpdateRequest("수정된 내용", List.of("BAD"), List.of("피곤함"), List.of(), DailyRecord.Status.COMPLETED);

        DailyRecordResponse response = dailyRecordService.update(1L, 1L, request);

        assertThat(response.content()).isEqualTo("수정된 내용");
        assertThat(response.mood()).isEqualTo(List.of("BAD"));
    }

    @Test
    void returnsRecordWhenDateHasRecord() {
        DailyRecord record = DailyRecord.create(1L, LocalDate.of(2026, 7, 9), "오늘 기록", List.of("GOOD"), List.of(), List.of(), DailyRecord.Status.COMPLETED);
        when(dailyRecordRepository.findByUserIdAndRecordDateAndDeletedAtIsNull(1L, LocalDate.of(2026, 7, 9)))
                .thenReturn(Optional.of(record));

        DailyRecordResponse response = dailyRecordService.getByDate(1L, LocalDate.of(2026, 7, 9));

        assertThat(response.content()).isEqualTo("오늘 기록");
    }

    @Test
    void throwsDuplicateResourceOnConcurrentCreate() {
        DailyRecordCreateRequest request = new DailyRecordCreateRequest(
                LocalDate.of(2026, 7, 9), "동시 작성 시도", List.of("GOOD"), List.of(), List.of(), DailyRecord.Status.COMPLETED
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

    // 리스트를 제대로 반환하는지 테스트
    @Test
    void returnsRecordsWhenTagMatches() {
        DailyRecord record = DailyRecord.create(1L, LocalDate.of(2026, 7, 9), "오늘 기록", List.of("GOOD"), List.of("따뜻해요"), List.of("로맨스"), DailyRecord.Status.COMPLETED);
        when(dailyRecordRepository.findByTags(1L, null, "따뜻해요", null))
                .thenReturn(List.of(record));

        List<DailyRecordResponse> response = dailyRecordService.getByTags(1L, null, "따뜻해요", null);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).moodTags()).isEqualTo(List.of("따뜻해요"));
    }

    // 셋다 null로 호출했을때 진짜 400_3 오류가 발생하는지 테스트
    @Test
    void throwsMissingParameterWhenNoTagGiven() {
        assertThatThrownBy(() -> dailyRecordService.getByTags(1L, null, null, null))
                .isInstanceOf(GeneralException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorStatus.MISSING_PARAMETER);
    }

    @Test
    void returnsRecordWhenIdExists() {
        DailyRecord record = DailyRecord.create(1L, LocalDate.of(2026, 7, 9), "오늘 기록", List.of("GOOD"), List.of(), List.of(), DailyRecord.Status.COMPLETED);
        when(dailyRecordRepository.findByIdAndUserIdAndStatusAndDeletedAtIsNull(1L, 1L, DailyRecord.Status.COMPLETED))
                .thenReturn(Optional.of(record));

        DailyRecordResponse response = dailyRecordService.getById(1L, 1L);

        assertThat(response.content()).isEqualTo("오늘 기록");
    }

    @Test
    void throwsNotFoundWhenIdDoesNotExist() {
        when(dailyRecordRepository.findByIdAndUserIdAndStatusAndDeletedAtIsNull(999L, 1L, DailyRecord.Status.COMPLETED))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> dailyRecordService.getById(1L, 999L))
                .isInstanceOf(GeneralException.class);
    }

    @Test
    // 오늘 기록이 아예 없을 때 INCOMPLETE  + recordId:null 확인
    void returnsIncompleteWhenNoRecordToday() {
        when(dailyRecordRepository.findByUserIdAndRecordDateAndDeletedAtIsNull(eq(1L), any(LocalDate.class)))
                .thenReturn(Optional.empty());

        TodayStatusResponse response = dailyRecordService.getTodayStatus(1L);

        assertThat(response.screen()).isEqualTo(TodayScreen.INCOMPLETE);
        assertThat(response.recordId()).isNull();
    }

    @Test
    // 기록이 DRAFT 상태일 때 INCOMPLETE 반환 확인
    void returnsIncompleteWhenRecordIsDraft() {
        DailyRecord record = DailyRecord.create(1L, LocalDate.now(), "쓰다 만 기록", List.of(), List.of(), List.of(), DailyRecord.Status.DRAFT);
        when(dailyRecordRepository.findByUserIdAndRecordDateAndDeletedAtIsNull(eq(1L), any(LocalDate.class)))
                .thenReturn(Optional.of(record));

        TodayStatusResponse response = dailyRecordService.getTodayStatus(1L);

        assertThat(response.screen()).isEqualTo(TodayScreen.INCOMPLETE);
    }

    @Test
    // 기록은 COMPLETED인데 분석 요청 자체가 아직 없을 때(AiAnalysisLog 없음) WAITING 반환 확인
    void returnsWaitingWhenAnalysisNotRequestedYet() {
        DailyRecord record = DailyRecord.create(1L, LocalDate.now(), "완료된 기록", List.of(), List.of(), List.of(), DailyRecord.Status.COMPLETED);
        when(dailyRecordRepository.findByUserIdAndRecordDateAndDeletedAtIsNull(eq(1L), any(LocalDate.class)))
                .thenReturn(Optional.of(record));
        when(aiAnalysisLogRepository.findByDailyRecordId(any())).thenReturn(Optional.empty());

        TodayStatusResponse response = dailyRecordService.getTodayStatus(1L);

        assertThat(response.screen()).isEqualTo(TodayScreen.WAITING);
    }

    @Test
    // 분석이 PENDING 상태일 때 WAITING 반환 확인
    void returnsWaitingWhenAnalysisPending() {
        DailyRecord record = DailyRecord.create(1L, LocalDate.now(), "완료된 기록", List.of(), List.of(), List.of(), DailyRecord.Status.COMPLETED);
        AiAnalysisLog log = AiAnalysisLog.builder().dailyRecordId(1L).provider("OPENAI").model("gpt").prompt("p").build();
        when(dailyRecordRepository.findByUserIdAndRecordDateAndDeletedAtIsNull(eq(1L), any(LocalDate.class)))
                .thenReturn(Optional.of(record));
        when(aiAnalysisLogRepository.findByDailyRecordId(any())).thenReturn(Optional.of(log));

        TodayStatusResponse response = dailyRecordService.getTodayStatus(1L);

        assertThat(response.screen()).isEqualTo(TodayScreen.WAITING);
    }

    @Test
    // 분석이 FAILED 상태일 때 FAILED 반환 확인
    void returnsFailedWhenAnalysisFailed() {
        DailyRecord record = DailyRecord.create(1L, LocalDate.now(), "완료된 기록", List.of(), List.of(), List.of(), DailyRecord.Status.COMPLETED);
        AiAnalysisLog log = AiAnalysisLog.builder().dailyRecordId(1L).provider("OPENAI").model("gpt").prompt("p").build();
        log.markFailed("에러 발생");
        when(dailyRecordRepository.findByUserIdAndRecordDateAndDeletedAtIsNull(eq(1L), any(LocalDate.class)))
                .thenReturn(Optional.of(record));
        when(aiAnalysisLogRepository.findByDailyRecordId(any())).thenReturn(Optional.of(log));

        TodayStatusResponse response = dailyRecordService.getTodayStatus(1L);

        assertThat(response.screen()).isEqualTo(TodayScreen.FAILED);
    }

    @Test
    // 분석은 SUCCESS인데 캐스팅 카드가 아직 안 만들어졌을 때 WAITING으로 방어적 처리되는지 확인
    void returnsWaitingWhenAnalysisSuccessButNoCastingCardYet() {
        DailyRecord record = DailyRecord.create(1L, LocalDate.now(), "완료된 기록", List.of(), List.of(), List.of(), DailyRecord.Status.COMPLETED);
        AiAnalysisLog log = AiAnalysisLog.builder().dailyRecordId(1L).provider("OPENAI").model("gpt").prompt("p").build();
        log.markSuccess("{}");
        when(dailyRecordRepository.findByUserIdAndRecordDateAndDeletedAtIsNull(eq(1L), any(LocalDate.class)))
                .thenReturn(Optional.of(record));
        when(aiAnalysisLogRepository.findByDailyRecordId(any())).thenReturn(Optional.of(log));
        when(castingCardRepository.findByDailyRecordId(any())).thenReturn(Optional.empty());

        TodayStatusResponse response = dailyRecordService.getTodayStatus(1L);

        assertThat(response.screen()).isEqualTo(TodayScreen.WAITING);
    }

    @Test
    // 분석 SUCCESS + 카드 존재까지 확인됐을 때 RESULT 반환 확인
    void returnsResultWhenAnalysisSuccessAndCastingCardExists() {
        DailyRecord record = DailyRecord.create(1L, LocalDate.now(), "완료된 기록", List.of(), List.of(), List.of(), DailyRecord.Status.COMPLETED);
        AiAnalysisLog log = AiAnalysisLog.builder().dailyRecordId(1L).provider("OPENAI").model("gpt").prompt("p").build();
        log.markSuccess("{}");
        CastingCard card = CastingCard.builder().dailyRecordId(1L).genre("드라마").roleName("오늘의 주인공").build();
        when(dailyRecordRepository.findByUserIdAndRecordDateAndDeletedAtIsNull(eq(1L), any(LocalDate.class)))
                .thenReturn(Optional.of(record));
        when(aiAnalysisLogRepository.findByDailyRecordId(any())).thenReturn(Optional.of(log));
        when(castingCardRepository.findByDailyRecordId(any())).thenReturn(Optional.of(card));

        TodayStatusResponse response = dailyRecordService.getTodayStatus(1L);

        assertThat(response.screen()).isEqualTo(TodayScreen.RESULT);
    }
}