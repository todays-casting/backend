package com.todayscasting.domain.casting.converter;

import com.todayscasting.domain.casting.dto.response.CastingCardResponseDTO;
import com.todayscasting.domain.casting.entity.CastingCard;

public class CastingCardConverter {

    private CastingCardConverter() {
    }

    public static CastingCardResponseDTO toResponseDTO(CastingCard castingCard, String imageUrl) {
        return CastingCardResponseDTO.builder()
                .id(castingCard.getId())
                .dailyRecordId(castingCard.getDailyRecordId())
                .castingImageId(castingCard.getCastingImageId())
                .imageUrl(imageUrl)
                .imageKey(castingCard.getGeneratedImageKey())
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
