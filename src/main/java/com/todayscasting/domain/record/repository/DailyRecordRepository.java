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

    Optional<DailyRecord> findByIdAndDeletedAtIsNull(Long id);

    Optional<DailyRecord> findByIdAndUserIdAndDeletedAtIsNull(Long id, Long userId);

    // 삭제된거까지 포함해서 찾는 조회
    Optional<DailyRecord> findByUserIdAndRecordDate(Long userId, LocalDate recordDate);

    // mood, moodTag, activityTag로 기록을 검색하는 쿼리 메서드
    @Query(value = """
    SELECT * FROM daily_records
    WHERE user_id = :userId
      AND deleted_at IS NULL
      AND (:mood IS NULL OR JSON_CONTAINS(mood, JSON_QUOTE(:mood)))
      AND (:moodTag IS NULL OR JSON_CONTAINS(mood_tags, JSON_QUOTE(:moodTag)))
      AND (:activityTag IS NULL OR JSON_CONTAINS(activity_tags, JSON_QUOTE(:activityTag)))
    """, nativeQuery = true)
    List<DailyRecord> findByTags(
            @Param("userId") Long userId,
            @Param("mood") String mood,
            @Param("moodTag") String moodTag,
            @Param("activityTag") String activityTag
    );

}