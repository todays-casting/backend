package com.todayscasting.domain.record.service;

import com.todayscasting.domain.record.dto.request.DailyRecordCreateRequest;
import com.todayscasting.domain.record.dto.request.DailyRecordUpdateRequest;
import com.todayscasting.domain.record.dto.response.DailyRecordResponse;

import java.time.LocalDate;
import java.util.List;

public interface DailyRecordService {

    DailyRecordResponse create(Long userId, DailyRecordCreateRequest request);

    DailyRecordResponse update(Long userId, Long recordId, DailyRecordUpdateRequest request);

    void delete(Long userId, Long recordId);

    DailyRecordResponse getByDate(Long userId, LocalDate date);

    List<DailyRecordResponse> getByTags(Long userId, String mood, String moodTag, String activityTag);

    DailyRecordResponse getById(Long userId, Long recordId);
}