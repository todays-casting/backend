package com.todayscasting.domain.record.converter;

import com.todayscasting.domain.casting.entity.CastingCard;
import com.todayscasting.domain.record.dto.request.DailyRecordCreateRequest;
import com.todayscasting.domain.record.dto.response.CalendarResponse;
import com.todayscasting.domain.record.dto.response.DailyRecordResponse;
import com.todayscasting.domain.record.dto.response.HistoryCardResponse;
import com.todayscasting.domain.record.entity.DailyRecord;

public class DailyRecordConverter {

    private DailyRecordConverter() {
    }

    public static DailyRecord toEntity(Long userId, DailyRecordCreateRequest request) {
        return DailyRecord.create(
                userId,
                request.recordDate(),
                request.content(),
                request.mood(),
                request.moodTags(),
                request.activityTags()
        );
    }

    public static DailyRecordResponse toResponse(DailyRecord dailyRecord) {
        return new DailyRecordResponse(
                dailyRecord.getId(),
                dailyRecord.getRecordDate(),
                dailyRecord.getContent(),
                dailyRecord.getMood(),
                dailyRecord.getMoodTags(),
                dailyRecord.getActivityTags(),
                dailyRecord.getCreatedAt(),
                dailyRecord.getUpdatedAt()
        );
    }

    public static CalendarResponse toCalendarResponse(DailyRecord dailyRecord, boolean isFavorite) {
        return new CalendarResponse(
                dailyRecord.getRecordDate(),
                true, // 호출되는 시점에 이미 기록이 존재하는게 확정임
                isFavorite
        );
    }

    public static HistoryCardResponse toHistoryCardResponse(DailyRecord dailyRecord, CastingCard castingCard) {
        return new HistoryCardResponse(
                dailyRecord.getId(),
                dailyRecord.getRecordDate(),
                dailyRecord.getMood(),
                dailyRecord.getContent(),
                castingCard.getTitle(),
                castingCard.getGenre(),
                castingCard.getRoleName(),
                castingCard.getHighlight(),
                castingCard.getOneLineComment(),
                castingCard.getIsFavorite()
        );
    }
}