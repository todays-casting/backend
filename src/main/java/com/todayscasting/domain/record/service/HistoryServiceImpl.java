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
                .findByUserIdAndRecordDateBetweenAndDeletedAtIsNullOrderByRecordDateAsc(userId, startDate, endDate);

        if (records.isEmpty()) {
            return List.of();
        }

        List<Long> recordIds = records.stream()
                .map(DailyRecord::getId)
                .toList();

        // recordId -> CastingCard 매핑 (없는 recordId는 이 맵에 아예 안 들어감)
        Map<Long, CastingCard> castingCardByRecordId = castingCardRepository
                .findByDailyRecordIdIn(recordIds)
                .stream()
                .collect(Collectors.toMap(CastingCard::getDailyRecordId, castingCard -> castingCard));

        return records.stream()
                // DailyRecord 목록을 돌면서, 캐스팅카드가 있는 record인지 하나씩 확인
                .filter(record -> castingCardByRecordId.containsKey(record.getId()))
                .map(record -> DailyRecordConverter.toHistoryCardResponse(record, castingCardByRecordId.get(record.getId())))
                .toList();
    }
}