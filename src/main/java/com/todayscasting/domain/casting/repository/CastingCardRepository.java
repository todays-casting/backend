package com.todayscasting.domain.casting.repository;

import com.todayscasting.domain.casting.entity.CastingCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CastingCardRepository extends JpaRepository<CastingCard, Long> {

    Optional<CastingCard> findByDailyRecordId(Long dailyRecordId);

    // 기록된 날짜들(dailyRecordIds) 중에서 즐겨찾기(하트) 표시된 것만 필터링
    List<CastingCard> findByDailyRecordIdInAndIsFavoriteTrue(List<Long> dailyRecordIds);

}