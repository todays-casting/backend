package com.todayscasting.domain.casting.converter;

import com.todayscasting.domain.casting.dto.response.CastingCardResponseDTO;
import com.todayscasting.domain.casting.entity.CastingCard;

public class CastingCardConverter {

    private CastingCardConverter() {
    }

    // imageUrl(기존 매칭 방식, 바로 쓸 수 있는 고정 URL)과 imageKey(실시간 생성 이미지의
    // S3 key, 프론트가 별도 API로 URL 교환 필요) 중 정확히 하나만 채워져서 넘어온다.
    // 계산 로직은 호출부(CastingCardServiceImpl)에서 미리 처리한다. (이슈 #93)
    public static CastingCardResponseDTO toResponseDTO(CastingCard castingCard, String imageUrl, String imageKey) {
        return CastingCardResponseDTO.builder()
                .id(castingCard.getId())
                .dailyRecordId(castingCard.getDailyRecordId())
                .castingImageId(castingCard.getCastingImageId())
                .imageUrl(imageUrl)
                .imageKey(imageKey)
                .genre(castingCard.getGenre())
                .roleName(castingCard.getRoleName())
                .highlight(castingCard.getHighlight())
                .oneLineComment(castingCard.getOneLineComment())
                .scenePhrase(castingCard.getScenePhrase())
                .commentPhrase(castingCard.getCommentPhrase())
                .additionalMood(castingCard.getAdditionalMood())
                .characterPhrase(castingCard.getCharacterPhrase())
                .isFavorite(castingCard.getIsFavorite())
                .generatedAt(castingCard.getGeneratedAt())
                .createdAt(castingCard.getCreatedAt())
                .updatedAt(castingCard.getUpdatedAt())
                .build();
    }

}