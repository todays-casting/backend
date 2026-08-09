package com.todayscasting.domain.casting.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class CastingCardResponseDTO {

    private Long id;
    private Long dailyRecordId;
    private Long castingImageId;
    private String genre;
    private String roleName;
    private String highlight;
    private String oneLineComment;
    private String scenePhrase;
    private String commentPhrase;
    private List<String> additionalMood;
    private String characterPhrase;
    private Boolean isFavorite;
    private LocalDateTime generatedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}