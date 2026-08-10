package com.todayscasting.domain.casting.service;

import com.todayscasting.domain.casting.dto.request.CastingCardRequestDTO;
import com.todayscasting.domain.casting.dto.response.CastingCardResponseDTO;
import com.todayscasting.domain.casting.dto.response.CastingFavoriteCountResponseDTO;
import com.todayscasting.domain.casting.dto.response.CastingFavoriteResponseDTO;

import java.util.List;

public interface CastingCardService {

    CastingCardResponseDTO createCastingCard(Long userId, CastingCardRequestDTO request);

    CastingCardResponseDTO getCastingCard(Long userId, Long dailyRecordId);

    CastingCardResponseDTO toggleFavorite(Long userId, Long dailyRecordId);

    CastingFavoriteCountResponseDTO getFavoriteCount(Long userId);

    List<CastingFavoriteResponseDTO> getFavoriteList(Long userId);

}