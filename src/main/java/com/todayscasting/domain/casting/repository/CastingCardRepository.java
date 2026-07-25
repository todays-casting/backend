package com.todayscasting.domain.casting.repository;

import com.todayscasting.domain.casting.entity.CastingCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CastingCardRepository extends JpaRepository<CastingCard, Long> {

    Optional<CastingCard> findByDailyRecordId(Long dailyRecordId);

}