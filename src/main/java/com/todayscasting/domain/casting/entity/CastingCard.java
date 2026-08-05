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

    // title은 확정된 UI(오늘의 캐스팅 결과, 히스토리 조회)에 노출되지 않아
    // 더 이상 AI 분석 시 값을 생성/저장하지 않기로 결정 (2026-08-04) -> nullable로 변경 (V4 마이그레이션)
    @Column(length = 100)
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

    // "오늘의 캐스팅 결과" 화면 전용 필드. highlight/oneLineComment와 달리
    // 완결된 문장이 아니라 짧고 함축적인 "문구" 스타일로 생성됨 (2026-08-05, V5 마이그레이션)
    @Column(name = "scene_phrase", length = 100)
    private String scenePhrase;

    @Column(name = "comment_phrase", length = 100)
    private String commentPhrase;

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
                        String scenePhrase, String commentPhrase,
                        Integer score, String analysisSummary) {
        this.dailyRecordId = dailyRecordId;
        this.title = title;
        this.subtitle = subtitle;
        this.genre = genre;
        this.roleName = roleName;
        this.highlight = highlight;
        this.oneLineComment = oneLineComment;
        this.scenePhrase = scenePhrase;
        this.commentPhrase = commentPhrase;
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