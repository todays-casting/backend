package com.todayscasting.domain.casting.converter;

import com.todayscasting.domain.casting.dto.response.CastingCardResponseDTO;
import com.todayscasting.domain.casting.entity.CastingCard;
import com.todayscasting.domain.casting.support.CastingImageResolver;
import com.todayscasting.domain.user.entity.User;

public class CastingCardConverter {

    private CastingCardConverter() {
    }

    // 카드의 genre와 카드 소유자의 성별을 함께 받아 imageUrl까지 채운 응답을 만든다. (이슈 #89)
    public static CastingCardResponseDTO toResponseDTO(CastingCard castingCard, User.Gender gender) {
        String imageUrl = CastingImageResolver.resolveImageUrl(castingCard.getGenre(), gender);

        return CastingCardResponseDTO.builder()
                .id(castingCard.getId())
                .dailyRecordId(castingCard.getDailyRecordId())
                .castingImageId(castingCard.getCastingImageId())
                .imageUrl(imageUrl)
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