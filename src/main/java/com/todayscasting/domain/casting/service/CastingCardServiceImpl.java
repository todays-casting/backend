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
import com.todayscasting.domain.casting.dto.response.CastingFavoriteCountResponseDTO;
import com.todayscasting.domain.casting.entity.CastingCard;
import com.todayscasting.domain.casting.repository.CastingCardRepository;
import com.todayscasting.domain.record.repository.DailyRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CastingCardServiceImpl implements CastingCardService {

    private final CastingCardRepository castingCardRepository;
    private final AiAnalysisLogRepository aiAnalysisLogRepository;
    private final DailyRecordRepository dailyRecordRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public CastingCardResponseDTO createCastingCard(Long userId, CastingCardRequestDTO request) {

        // 본인 소유의 하루 기록인지 먼저 검증 (다른 사용자의 dailyRecordId로 캐스팅 카드를 만들지 못하게 차단)
        validateOwnership(userId, request.getDailyRecordId());

        // 같은 dailyRecordId로 이미 캐스팅 카드가 있으면 중복 생성 차단 (유니크 제약 위반이 그대로 500으로 새는 것 방지)
        if (castingCardRepository.findByDailyRecordId(request.getDailyRecordId()).isPresent()) {
            throw new GeneralException(ErrorStatus.INVALID_REQUEST);
        }

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
                .genre(getTextOrDefault(analysisResult, "genre", "일상 드라마"))
                .roleName(getTextOrDefault(analysisResult, "roleName", "오늘의 주인공"))
                .highlight(getTextOrDefault(analysisResult, "highlight", null))
                .oneLineComment(getTextOrDefault(analysisResult, "oneLineComment", null))
                .scenePhrase(getTextOrDefault(analysisResult, "scenePhrase", null))
                .commentPhrase(getTextOrDefault(analysisResult, "commentPhrase", null))
                .additionalMood(getStringListOrEmpty(analysisResult, "additionalMood"))
                .characterPhrase(getTextOrDefault(analysisResult, "characterPhrase", null))
                .build();

        CastingCard savedCastingCard;
        try {
            savedCastingCard = castingCardRepository.save(castingCard);
        } catch (DataIntegrityViolationException e) {
            // 동시 요청으로 인한 경합(race condition)까지 대비하는 이중 안전장치
            throw new GeneralException(ErrorStatus.INVALID_REQUEST);
        }

        return CastingCardConverter.toResponseDTO(savedCastingCard);
    }

    // 요청한 dailyRecordId가 실제로 이 userId 소유인지 확인. 아니면(또는 존재하지 않으면) 404로 처리해
    // "이 ID는 존재하지만 남의 것"이라는 정보 자체가 새어나가지 않도록 함
    private void validateOwnership(Long userId, Long dailyRecordId) {
        dailyRecordRepository.findByIdAndUserIdAndDeletedAtIsNull(dailyRecordId, userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.RESOURCE_NOT_FOUND));
    }

    private JsonNode parseAnalysisResult(String rawResponse) {
        JsonNode node;
        try {
            node = objectMapper.readTree(rawResponse);
        } catch (Exception e) {
            throw new GeneralException(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
        // 배열이나 단순 값(primitive)이 아니라 객체(object) 형태여야만 유효한 분석 결과로 취급
        if (!node.isObject()) {
            throw new GeneralException(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
        return node;
    }

    private String getTextOrDefault(JsonNode node, String field, String defaultValue) {
        if (node.hasNonNull(field)) {
            return node.get(field).asText();
        }
        return defaultValue;
    }

    // additionalMood처럼 AI가 배열로 응답하는 필드를 안전하게 List<String>으로 변환.
    // 필드가 없거나 배열이 아니면 빈 리스트로 처리 (AI가 형식을 안 지켜도 에러 대신 안전하게 처리)
    private List<String> getStringListOrEmpty(JsonNode node, String field) {
        List<String> result = new ArrayList<>();
        if (node.hasNonNull(field) && node.get(field).isArray()) {
            node.get(field).forEach(item -> {
                if (item.isTextual() && !item.asText().isBlank()) {
                    result.add(item.asText());
                }
            });
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public CastingCardResponseDTO getCastingCard(Long userId, Long dailyRecordId) {
        validateOwnership(userId, dailyRecordId);
        CastingCard castingCard = findByDailyRecordIdOrThrow(dailyRecordId);
        return CastingCardConverter.toResponseDTO(castingCard);
    }

    @Override
    @Transactional
    public CastingCardResponseDTO toggleFavorite(Long userId, Long dailyRecordId) {
        validateOwnership(userId, dailyRecordId);
        CastingCard castingCard = findByDailyRecordIdOrThrow(dailyRecordId);
        castingCard.toggleFavorite();
        return CastingCardConverter.toResponseDTO(castingCard);
    }

    private CastingCard findByDailyRecordIdOrThrow(Long dailyRecordId) {
        return castingCardRepository.findByDailyRecordId(dailyRecordId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.RESOURCE_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public CastingFavoriteCountResponseDTO getFavoriteCount(Long userId) {
        long count = castingCardRepository.countFavoritesByUserId(userId);
        return CastingFavoriteCountResponseDTO.builder()
                .favoriteCount(count)
                .build();
    }

}