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

    // recordId, userId로 삭제되지 않은 dailyRecord를 찾음 (update/delete용, status 무관)
    Optional<DailyRecord> findByIdAndUserIdAndDeletedAtIsNull(Long id, Long userId);

    // recordId, userId, status(COMPLETED)로 삭제되지 않은 dailyRecord를 찾음 (getById 전용, draft는 안 보임)
    Optional<DailyRecord> findByIdAndUserIdAndStatusAndDeletedAtIsNull(Long id, Long userId, DailyRecord.Status status);

    // 삭제된거까지 포함해서 찾는 조회
    Optional<DailyRecord> findByUserIdAndRecordDate(Long userId, LocalDate recordDate);

    // mood, moodTag, activityTag로 기록을 검색하는 쿼리 메서드
    // mood/activityTag는 JSON 배열 문자열(다중값, AND매칭), moodTag는 단일값
    // status='COMPLETED' 하드코딩: 태그 검색은 완료된 기록만 대상으로 함(draft 검색 필요 없음)
    @Query(value = """
    SELECT * FROM daily_records
    WHERE user_id = :userId
    AND deleted_at IS NULL
    AND status = 'COMPLETED'
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

    // 캘린더/히스토리 공용: 특정 유저의 월범위(start~end) + status로 기록 조회, 날짜 오름차순
    List<DailyRecord> findByUserIdAndRecordDateBetweenAndStatusAndDeletedAtIsNullOrderByRecordDateAsc(
            Long userId, LocalDate startDate, LocalDate endDate, DailyRecord.Status status
    );
}