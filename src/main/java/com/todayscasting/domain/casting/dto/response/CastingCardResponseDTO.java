package com.todayscasting.domain.casting.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CastingCardResponseDTO {

    private Long id;
    private Long dailyRecordId;
    private Long castingImageId;
    private String title;
    private String subtitle;
    private String genre;
    private String roleName;
    private String highlight;
    private String oneLineComment;
    private Integer score;
    private String analysisSummary;
    private Boolean isFavorite;
    private LocalDateTime generatedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}