package com.todayscasting.domain.casting.entity;

import com.todayscasting.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "casting_cards")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CastingCard extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "daily_record_id", nullable = false)
    private Long dailyRecordId;

    @Column(name = "casting_image_id")
    private Long castingImageId;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 100)
    private String subtitle;

    @Column(nullable = false, length = 50)
    private String genre;

    @Column(name = "role_name", nullable = false, length = 100)
    private String roleName;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String highlight;

    @Column(name = "one_line_comment", columnDefinition = "TEXT")
    private String oneLineComment;

    @Column
    private Integer score;

    @Column(name = "analysis_summary", columnDefinition = "TEXT")
    private String analysisSummary;

    @Column(name = "is_favorite", nullable = false)
    private Boolean isFavorite;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    @Builder
    private CastingCard(Long dailyRecordId, String title, String subtitle, String genre,
                        String roleName, String highlight, String oneLineComment,
                        Integer score, String analysisSummary) {
        this.dailyRecordId = dailyRecordId;
        this.title = title;
        this.subtitle = subtitle;
        this.genre = genre;
        this.roleName = roleName;
        this.highlight = highlight;
        this.oneLineComment = oneLineComment;
        this.score = score;
        this.analysisSummary = analysisSummary;
        this.isFavorite = false;
        this.generatedAt = LocalDateTime.now();
    }

    public void toggleFavorite() {
        this.isFavorite = !this.isFavorite;
    }

    public void linkCastingImage(Long castingImageId) {
        this.castingImageId = castingImageId;
    }

}