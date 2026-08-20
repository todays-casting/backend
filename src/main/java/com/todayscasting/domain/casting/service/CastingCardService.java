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

    // imageKey(S3 객체 key)를 받아서, 화면에 그리기 직전에 쓸 presigned URL을 새로 발급한다. (이슈 #93)
    String getImageUrl(String imageKey);

    // 오늘의 결과 화면 "오늘의 카드 다운로드" 버튼용. 배경 이미지 위에 날짜/배역명만 합성한
    // 다운로드 전용 이미지를 만들어 presigned URL로 반환한다.
    String generateDownloadCardImage(Long userId, Long dailyRecordId);

}