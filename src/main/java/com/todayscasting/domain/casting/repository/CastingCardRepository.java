package com.todayscasting.domain.casting.repository;

import com.todayscasting.domain.casting.entity.CastingCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CastingCardRepository extends JpaRepository<CastingCard, Long> {

    Optional<CastingCard> findByDailyRecordId(Long dailyRecordId);

    // 기록된 날짜들(dailyRecordIds) 중에서 즐겨찾기(하트) 표시된 것만 필터링
    List<CastingCard> findByDailyRecordIdInAndIsFavoriteTrue(List<Long> dailyRecordIds);

    // 히스토리 목록용: 기록된 날짜들(dailyRecordIds) 중 캐스팅카드가 존재하는 것 전체 조회
    // (즐겨찾기 여부 상관없음 — 캐스팅카드 없는 날 제외하는 INNER JOIN 로직에 사용)
    List<CastingCard> findByDailyRecordIdIn(List<Long> dailyRecordIds);

    // 마이페이지 "찜한 카드" 개수 조회용. casting_cards엔 user_id가 없어 daily_records와 조인해서
    // 해당 유저 소유의 기록 중 즐겨찾기된 캐스팅 카드 개수만 센다.
    @Query("""
            SELECT COUNT(c) FROM CastingCard c
            JOIN DailyRecord d ON c.dailyRecordId = d.id
            WHERE d.userId = :userId
              AND c.isFavorite = true
              AND d.deletedAt IS NULL
            """)
    long countFavoritesByUserId(@Param("userId") Long userId);

}