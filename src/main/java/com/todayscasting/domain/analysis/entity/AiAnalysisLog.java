package com.todayscasting.domain.analysis.entity;

import com.todayscasting.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "ai_analysis_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiAnalysisLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "daily_record_id", nullable = false)
    private Long dailyRecordId;

    @Column(nullable = false, length = 30)
    private String provider;

    @Column(length = 100)
    private String model;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String prompt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_response", columnDefinition = "json")
    private String rawResponse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AnalysisStatus status;

    @Lob
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Builder
    private AiAnalysisLog(Long dailyRecordId, String provider, String model, String prompt) {
        this.dailyRecordId = dailyRecordId;
        this.provider = provider;
        this.model = model;
        this.prompt = prompt;
        this.status = AnalysisStatus.PENDING;
    }

    public void markSuccess(String rawResponse) {
        this.status = AnalysisStatus.SUCCESS;
        this.rawResponse = rawResponse;
        this.errorMessage = null;
    }

    public void markFailed(String errorMessage) {
        this.status = AnalysisStatus.FAILED;
        this.rawResponse = null;
        this.errorMessage = errorMessage;
    }

    public void retry(String newPrompt) {
        this.status = AnalysisStatus.PENDING;
        this.prompt = newPrompt;
        this.rawResponse = null;
        this.errorMessage = null;
    }
}