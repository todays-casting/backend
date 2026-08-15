package com.todayscasting.domain.casting.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class CastingFavoriteResponseDTO {

    private Long dailyRecordId;
    private String genre;
    private String roleName;
    private String highlight;
    private String oneLineComment;
    private String imageUrl;
    private List<String> additionalMood;
    private Boolean isFavorite;
    private LocalDateTime generatedAt;

}