package com.todayscasting.domain.record.service;

import com.todayscasting.common.code.status.ErrorStatus;
import com.todayscasting.common.exception.GeneralException;
import com.todayscasting.domain.casting.entity.CastingCard;
import com.todayscasting.domain.casting.repository.CastingCardRepository;
import com.todayscasting.domain.record.converter.DailyRecordConverter;
import com.todayscasting.domain.record.dto.response.HistoryCardResponse;
import com.todayscasting.domain.record.entity.DailyRecord;
import com.todayscasting.domain.record.repository.DailyRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HistoryServiceImpl implements HistoryService {

    private final DailyRecordRepository dailyRecordRepository;
    private final CastingCardRepository castingCardRepository;

    @Override
    public List<HistoryCardResponse> getHistory(Long userId, LocalDate startDate, LocalDate endDate) {

        // startDate가 endDate보다 늦었을때의 예외처리
        if (startDate.isAfter(endDate)) {
            throw new GeneralException(ErrorStatus.INVALID_INPUT);
        }

        List<DailyRecord> records = dailyRecordRepository
                .findByUserIdAndRecordDateBetweenAndStatusAndDeletedAtIsNullOrderByRecordDateAsc(
                        userId, startDate, endDate, DailyRecord.Status.COMPLETED);

        if (records.isEmpty()) {
            return List.of();
        }

        List<Long> recordIds = records.stream()
                .map(DailyRecord::getId)
                .toList();

        // recordId -> CastingCard 매핑. 카드가 아직 없는 기록도 히스토리에는 노출하고,
        // 캐스팅 관련 필드만 null로 내려준다.
        Map<Long, CastingCard> castingCardByRecordId = castingCardRepository
                .findByDailyRecordIdIn(recordIds)
                .stream()
                .collect(Collectors.toMap(CastingCard::getDailyRecordId, castingCard -> castingCard));

        return records.stream()
                .map(record -> DailyRecordConverter.toHistoryCardResponse(record, castingCardByRecordId.get(record.getId())))
                .toList();
    }
}
