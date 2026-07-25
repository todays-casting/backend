package com.todayscasting.domain.casting.service;

import com.todayscasting.domain.casting.dto.request.CastingCardRequestDTO;
import com.todayscasting.domain.casting.dto.response.CastingCardResponseDTO;

public interface CastingCardService {

    CastingCardResponseDTO createCastingCard(CastingCardRequestDTO request);

    CastingCardResponseDTO getCastingCard(Long dailyRecordId);

    CastingCardResponseDTO toggleFavorite(Long dailyRecordId);

}