package com.todayscasting.domain.record.service;

import com.todayscasting.common.code.status.ErrorStatus;
import com.todayscasting.common.exception.GeneralException;
import com.todayscasting.domain.record.converter.DailyRecordConverter;
import com.todayscasting.domain.record.dto.request.DailyRecordCreateRequest;
import com.todayscasting.domain.record.dto.request.DailyRecordUpdateRequest;
import com.todayscasting.domain.record.dto.response.DailyRecordResponse;
import com.todayscasting.domain.record.entity.DailyRecord;
import com.todayscasting.domain.record.repository.DailyRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DailyRecordServiceImpl implements DailyRecordService {

    private final DailyRecordRepository dailyRecordRepository;

    @Override
    @Transactional
    public DailyRecordResponse create(Long userId, DailyRecordCreateRequest request) {
        Optional<DailyRecord> existing = dailyRecordRepository.findByUserIdAndRecordDate(userId, request.recordDate());
        // 같은 날짜로 이미 작성된 행이 있는지 확인(삭제된것도 포함해서 찾음)
        if (existing.isPresent()) { // 삭제됐든 안됐든 DB에 데이터가 존재하면
            DailyRecord dailyRecord = existing.get();
            if (dailyRecord.isDeleted()) {
                // 삭제된 행이면 새로 만들지 않고 되살려서 재사용(restore 메서드 이용)
                dailyRecord.restore();
                dailyRecord.update(request.content(), request.mood(), request.moodTags(), request.activityTags());
                dailyRecordRepository.saveAndFlush(dailyRecord); // 즉시 DB에 반영해서 updatedAt 최신화
                return DailyRecordConverter.toResponse(dailyRecord);
            }
            // 삭제 안된 행인데 또 작성 시도를 하면 409 에러 코드로 막음
            throw new GeneralException(ErrorStatus.DUPLICATE_RESOURCE);
        }
        // 오늘 날짜로 작성된 기록이 아예 없는 경우 -> 원래대로 생성
        DailyRecord dailyRecord = DailyRecordConverter.toEntity(userId, request);
        try {
            DailyRecord saved = dailyRecordRepository.save(dailyRecord);
            return DailyRecordConverter.toResponse(saved);
        } catch (DataIntegrityViolationException e) {
            throw new GeneralException(ErrorStatus.DUPLICATE_RESOURCE);
        }
    }

    @Override
    @Transactional
    public DailyRecordResponse update(Long userId, Long recordId, DailyRecordUpdateRequest request) {
        DailyRecord dailyRecord = dailyRecordRepository.findByIdAndUserIdAndDeletedAtIsNull(recordId, userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.RESOURCE_NOT_FOUND));

        dailyRecord.update(request.content(), request.mood(), request.moodTags(), request.activityTags());
        dailyRecordRepository.saveAndFlush(dailyRecord); // 추가: 즉시 DB에 반영해서 updatedAt 최신화
        return DailyRecordConverter.toResponse(dailyRecord);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long recordId) {
        DailyRecord dailyRecord = dailyRecordRepository.findByIdAndUserIdAndDeletedAtIsNull(recordId, userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.RESOURCE_NOT_FOUND));

        dailyRecord.delete();
    }

    @Override
    public DailyRecordResponse getByDate(Long userId, LocalDate date) {
        DailyRecord dailyRecord = dailyRecordRepository.findByUserIdAndRecordDateAndDeletedAtIsNull(userId, date)
                .orElseThrow(() -> new GeneralException(ErrorStatus.RESOURCE_NOT_FOUND));

        return DailyRecordConverter.toResponse(dailyRecord);
    }

    @Override
    public DailyRecordResponse getById(Long userId, Long recordId) {
        DailyRecord dailyRecord = dailyRecordRepository.findByIdAndUserIdAndDeletedAtIsNull(recordId, userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.RESOURCE_NOT_FOUND));

        return DailyRecordConverter.toResponse(dailyRecord);
    }

    @Override
    public List<DailyRecordResponse> getByTags(Long userId, String mood, String moodTag, String activityTag) {
        // 셋 다 값이 없거나(null) 빈 문자열/공백이면 400_3 오류 발생
        if (!StringUtils.hasText(mood) && !StringUtils.hasText(moodTag) && !StringUtils.hasText(activityTag)) {
            throw new GeneralException(ErrorStatus.MISSING_PARAMETER);
        }
        List<DailyRecord> records = dailyRecordRepository.findByTags(userId, mood, moodTag, activityTag);
        return records.stream()
                .map(DailyRecordConverter::toResponse)
                .toList();
    }
}