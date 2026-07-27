package com.todayscasting.domain.casting.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.todayscasting.common.code.status.ErrorStatus;
import com.todayscasting.common.exception.GeneralException;
import com.todayscasting.domain.analysis.entity.AiAnalysisLog;
import com.todayscasting.domain.analysis.entity.AnalysisStatus;
import com.todayscasting.domain.analysis.repository.AiAnalysisLogRepository;
import com.todayscasting.domain.casting.converter.CastingCardConverter;
import com.todayscasting.domain.casting.dto.request.CastingCardRequestDTO;
import com.todayscasting.domain.casting.dto.response.CastingCardResponseDTO;
import com.todayscasting.domain.casting.entity.CastingCard;
import com.todayscasting.domain.casting.repository.CastingCardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CastingCardServiceImpl implements CastingCardService {

    private final CastingCardRepository castingCardRepository;
    private final AiAnalysisLogRepository aiAnalysisLogRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public CastingCardResponseDTO createCastingCard(CastingCardRequestDTO request) {

        AiAnalysisLog analysisLog = aiAnalysisLogRepository
                .findByDailyRecordId(request.getDailyRecordId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.RESOURCE_NOT_FOUND));

        if (analysisLog.getStatus() != AnalysisStatus.SUCCESS || analysisLog.getRawResponse() == null) {
            // AI 분석이 아직 성공적으로 끝나지 않았으면 캐스팅 카드를 만들 수 없음
            throw new GeneralException(ErrorStatus.INVALID_REQUEST);
        }

        JsonNode analysisResult = parseAnalysisResult(analysisLog.getRawResponse());

        CastingCard castingCard = CastingCard.builder()
                .dailyRecordId(request.getDailyRecordId())
                .title(getTextOrDefault(analysisResult, "title", "오늘의 이야기"))
                .subtitle(getTextOrDefault(analysisResult, "subtitle", null))
                .genre(getTextOrDefault(analysisResult, "genre", "일상 드라마"))
                .roleName(getTextOrDefault(analysisResult, "roleName", "오늘의 주인공"))
                .highlight(getTextOrDefault(analysisResult, "highlight", null))
                .oneLineComment(getTextOrDefault(analysisResult, "oneLineComment", null))
                .score(getIntOrDefault(analysisResult, "score", 50))
                .analysisSummary(getTextOrDefault(analysisResult, "analysisSummary", null))
                .build();

        CastingCard savedCastingCard = castingCardRepository.save(castingCard);

        return CastingCardConverter.toResponseDTO(savedCastingCard);
    }

    private JsonNode parseAnalysisResult(String rawResponse) {
        try {
            return objectMapper.readTree(rawResponse);
        } catch (Exception e) {
            throw new GeneralException(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String getTextOrDefault(JsonNode node, String field, String defaultValue) {
        if (node.hasNonNull(field)) {
            return node.get(field).asText();
        }
        return defaultValue;
    }

    private Integer getIntOrDefault(JsonNode node, String field, Integer defaultValue) {
        if (node.hasNonNull(field)) {
            try {
                return Integer.parseInt(node.get(field).asText());
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    @Override
    @Transactional(readOnly = true)
    public CastingCardResponseDTO getCastingCard(Long dailyRecordId) {
        CastingCard castingCard = findByDailyRecordIdOrThrow(dailyRecordId);
        return CastingCardConverter.toResponseDTO(castingCard);
    }

    @Override
    @Transactional
    public CastingCardResponseDTO toggleFavorite(Long dailyRecordId) {
        CastingCard castingCard = findByDailyRecordIdOrThrow(dailyRecordId);
        castingCard.toggleFavorite();
        return CastingCardConverter.toResponseDTO(castingCard);
    }

    private CastingCard findByDailyRecordIdOrThrow(Long dailyRecordId) {
        return castingCardRepository.findByDailyRecordId(dailyRecordId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.RESOURCE_NOT_FOUND));
    }

}