package com.todayscasting.domain.record.repository;

import com.todayscasting.domain.record.entity.DailyRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyRecordRepository extends JpaRepository<DailyRecord, Long> {

    // 삭제가 안된것들에서만 찾는 메서드
    Optional<DailyRecord> findByUserIdAndRecordDateAndDeletedAtIsNull(Long userId, LocalDate recordDate);

    // recordId, userId로 삭제되지 않은 dailyRecord를 찾음
    Optional<DailyRecord> findByIdAndUserIdAndDeletedAtIsNull(Long id, Long userId);

    // 삭제된거까지 포함해서 찾는 조회
    Optional<DailyRecord> findByUserIdAndRecordDate(Long userId, LocalDate recordDate);

    // mood, moodTag, activityTag로 기록을 검색하는 쿼리 메서드
// mood/activityTag는 JSON 배열 문자열(다중값, AND매칭), moodTag는 단일값
    @Query(value = """
    SELECT * FROM daily_records
    WHERE user_id = :userId
    AND deleted_at IS NULL
    AND (:mood IS NULL OR JSON_CONTAINS(mood, :mood))
    AND (:moodTag IS NULL OR JSON_CONTAINS(mood_tags, JSON_QUOTE(:moodTag)))
    AND (:activityTag IS NULL OR JSON_CONTAINS(activity_tags, :activityTag))
    """, nativeQuery = true)
    List<DailyRecord> findByTags(
            @Param("userId") Long userId,
            @Param("mood") String mood,
            @Param("moodTag") String moodTag,
            @Param("activityTag") String activityTag
    );

    // 캘린더 마커용: 특정 유저의 월범위(start~end) 기록 전체 조회, 날짜 오름차순
    List<DailyRecord> findByUserIdAndRecordDateBetweenAndDeletedAtIsNullOrderByRecordDateAsc(
            Long userId, LocalDate startDate, LocalDate endDate
    );

}