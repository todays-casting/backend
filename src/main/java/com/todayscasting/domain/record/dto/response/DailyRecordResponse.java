package com.todayscasting.domain.record.dto.response;

import com.todayscasting.domain.record.entity.DailyRecord;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record DailyRecordResponse(
        Long id,
        LocalDate recordDate,
        String content,
        List<String> mood,
        List<String> moodTags,
        List<String> activityTags,
        DailyRecord.Status status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}