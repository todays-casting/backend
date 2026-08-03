package com.todayscasting.domain.casting.service;

import com.todayscasting.domain.casting.dto.request.CastingCardRequestDTO;
import com.todayscasting.domain.casting.dto.response.CastingCardResponseDTO;

public interface CastingCardService {

    CastingCardResponseDTO createCastingCard(Long userId, CastingCardRequestDTO request);

    CastingCardResponseDTO getCastingCard(Long userId, Long dailyRecordId);

    CastingCardResponseDTO toggleFavorite(Long userId, Long dailyRecordId);

}