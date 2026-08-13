package com.todayscasting.domain.record.service;

import com.todayscasting.domain.analysis.entity.AiAnalysisLog;
import com.todayscasting.domain.analysis.entity.AnalysisStatus;
import com.todayscasting.domain.analysis.repository.AiAnalysisLogRepository;
import com.todayscasting.domain.casting.repository.CastingCardRepository;
import com.todayscasting.domain.record.dto.response.TodayScreen;
import com.todayscasting.domain.record.dto.response.TodayStatusResponse;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;
    private final AiAnalysisLogRepository aiAnalysisLogRepository;
    private final CastingCardRepository castingCardRepository;

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
                dailyRecord.update(request.content(), request.mood(), request.moodTags(), request.activityTags(), request.status());
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

        dailyRecord.update(request.content(), request.mood(), request.moodTags(), request.activityTags(), request.status());
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

    // 달력 탭 하단 "오늘의 기록 보기/작성하기" 블록 + 날짜 클릭 시 recordId 조회에서 쓰임
    @Override
    public DailyRecordResponse getByDate(Long userId, LocalDate date) {
        return dailyRecordRepository.findByUserIdAndRecordDateAndDeletedAtIsNull(userId, date)
                .map(DailyRecordConverter::toResponse)
                .orElse(null);
    }

    @Override
    public DailyRecordResponse getById(Long userId, Long recordId) {
        DailyRecord dailyRecord = dailyRecordRepository
                .findByIdAndUserIdAndStatusAndDeletedAtIsNull(recordId, userId, DailyRecord.Status.COMPLETED)
                .orElseThrow(() -> new GeneralException(ErrorStatus.RESOURCE_NOT_FOUND));

        return DailyRecordConverter.toResponse(dailyRecord);
    }

    @Override
    public List<DailyRecordResponse> getByTags(Long userId, List<String> mood, String moodTag, List<String> activityTag) {
        List<String> normalizedMood = normalize(mood);
        String normalizedMoodTag = StringUtils.hasText(moodTag) ? moodTag : null;
        List<String> normalizedActivityTag = normalize(activityTag);

        if (normalizedMood == null && normalizedMoodTag == null && normalizedActivityTag == null) {
            throw new GeneralException(ErrorStatus.MISSING_PARAMETER);
        }

        String moodJson = normalizedMood == null ? null : toJsonArray(normalizedMood);
        String activityTagJson = normalizedActivityTag == null ? null : toJsonArray(normalizedActivityTag);

        List<DailyRecord> records = dailyRecordRepository.findByTags(userId, moodJson, normalizedMoodTag, activityTagJson);
        return records.stream()
                .map(DailyRecordConverter::toResponse)
                .toList();
    }

    // null/빈 리스트에서 빈 문자열 원소를 걸러내고, 남는 게 없으면 null(필터 스킵)로 취급
    private List<String> normalize(List<String> values) {
        if (values == null) {
            return null;
        }
        List<String> filtered = values.stream()
                .filter(StringUtils::hasText)
                .toList();
        return filtered.isEmpty() ? null : filtered;
    }

    // List<String>을 JSON 배열 문자열(예: ["행복","설렘"])로 직렬화
    private String toJsonArray(List<String> values) {
        try {
            return objectMapper.writeValueAsString(values);
        } catch (JacksonException e) {
            throw new GeneralException(ErrorStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    @Override
    public TodayStatusResponse getTodayStatus(Long userId) {
        DailyRecord record = dailyRecordRepository
                .findByUserIdAndRecordDateAndDeletedAtIsNull(userId, LocalDate.now())
                .orElse(null);

        if (record == null || record.getStatus() == DailyRecord.Status.DRAFT) {
            Long recordId = record != null ? record.getId() : null;
            return new TodayStatusResponse(TodayScreen.INCOMPLETE, recordId);
        }

        AiAnalysisLog analysisLog = aiAnalysisLogRepository.findByDailyRecordId(record.getId()).orElse(null);
        if (analysisLog == null || analysisLog.getStatus() == AnalysisStatus.PENDING) {
            return new TodayStatusResponse(TodayScreen.WAITING, record.getId());
        }
        if (analysisLog.getStatus() == AnalysisStatus.FAILED) {
            return new TodayStatusResponse(TodayScreen.FAILED, record.getId());
        }

        boolean hasCastingCard = castingCardRepository.findByDailyRecordId(record.getId()).isPresent();
        TodayScreen screen = hasCastingCard ? TodayScreen.RESULT : TodayScreen.WAITING;
        return new TodayStatusResponse(screen, record.getId());
    }
}