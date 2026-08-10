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
import com.todayscasting.domain.casting.dto.response.CastingFavoriteResponseDTO;
import com.todayscasting.domain.casting.entity.CastingCard;
import com.todayscasting.domain.casting.repository.CastingCardRepository;
import com.todayscasting.domain.notification.service.PushNotificationService;
import com.todayscasting.domain.record.entity.DailyRecord;
import com.todayscasting.domain.record.repository.DailyRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class CastingCardServiceImpl implements CastingCardService {

    private static final int MAX_ADDITIONAL_MOOD_COUNT = 2;

    private final CastingCardRepository castingCardRepository;
    private final AiAnalysisLogRepository aiAnalysisLogRepository;
    private final DailyRecordRepository dailyRecordRepository;
    private final PushNotificationService pushNotificationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public CastingCardResponseDTO createCastingCard(Long userId, CastingCardRequestDTO request) {

        // 본인 소유의 하루 기록인지 먼저 검증 (다른 사용자의 dailyRecordId로 캐스팅 카드를 만들지 못하게 차단)
        DailyRecord dailyRecord = validateOwnership(userId, request.getDailyRecordId());

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
                .additionalMood(getAdditionalMoodOrEmpty(analysisResult, dailyRecord))
                .characterPhrase(getTextOrDefault(analysisResult, "characterPhrase", null))
                .build();

        CastingCard savedCastingCard;
        try {
            savedCastingCard = castingCardRepository.save(castingCard);
        } catch (DataIntegrityViolationException e) {
            // 동시 요청으로 인한 경합(race condition)까지 대비하는 이중 안전장치
            throw new GeneralException(ErrorStatus.INVALID_REQUEST);
        }

        notifyCastingCardReadyAfterCommit(userId, savedCastingCard.getDailyRecordId());

        return CastingCardConverter.toResponseDTO(savedCastingCard);
    }

    private void notifyCastingCardReadyAfterCommit(Long userId, Long dailyRecordId) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            sendCastingCardReadyNotification(userId, dailyRecordId);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                sendCastingCardReadyNotification(userId, dailyRecordId);
            }
        });
    }

    private void sendCastingCardReadyNotification(Long userId, Long dailyRecordId) {
        try {
            pushNotificationService.sendCastingCardReady(userId, dailyRecordId);
        } catch (RuntimeException e) {
            log.warn("Failed to send casting card ready notification. userId={}, dailyRecordId={}", userId, dailyRecordId, e);
        }
    }

    // 요청한 dailyRecordId가 실제로 이 userId 소유인지 확인. 아니면(또는 존재하지 않으면) 404로 처리해
    // "이 ID는 존재하지만 남의 것"이라는 정보 자체가 새어나가지 않도록 함.
    // 소유권 검증과 동시에, additionalMood 정제에 필요한 DailyRecord(사용자가 선택한 mood 포함)를 반환
    private DailyRecord validateOwnership(Long userId, Long dailyRecordId) {
        return dailyRecordRepository.findByIdAndUserIdAndDeletedAtIsNull(dailyRecordId, userId)
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

    // additionalMood 필드를 안전하게 List<String>으로 변환하되, 필드 계약(contract)을 코드 레벨에서도 강제한다.
    // AI가 프롬프트 지시(사용자가 이미 선택한 감정과 중복 금지, 최대 2개)를 어기고 응답하더라도
    // 잘못된 데이터가 그대로 저장되지 않도록 이중으로 방어한다. (CodeRabbit 리뷰 반영)
    private List<String> getAdditionalMoodOrEmpty(JsonNode node, DailyRecord dailyRecord) {
        List<String> rawValues = new ArrayList<>();
        if (node.hasNonNull("additionalMood") && node.get("additionalMood").isArray()) {
            node.get("additionalMood").forEach(item -> {
                if (item.isTextual() && !item.asText().isBlank()) {
                    rawValues.add(item.asText());
                }
            });
        }

        // 사용자가 이미 선택한 mood(오늘의 감정)는 additionalMood에서 제외
        Set<String> selectedMoods = dailyRecord.getMood() != null
                ? new LinkedHashSet<>(dailyRecord.getMood())
                : Set.of();

        // 중복 제거(순서 유지) + 이미 선택된 감정 제외 + 최대 2개까지만 반영
        List<String> filtered = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        for (String value : rawValues) {
            if (selectedMoods.contains(value) || seen.contains(value)) {
                continue;
            }
            seen.add(value);
            filtered.add(value);
            if (filtered.size() == MAX_ADDITIONAL_MOOD_COUNT) {
                break;
            }
        }
        return filtered;
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

    // 마이페이지 "저장한 카드" 목록용. 즐겨찾기한 캐스팅 카드 전체를 화면에 필요한 형태로 변환해 반환 (이슈 #72)
    @Override
    @Transactional(readOnly = true)
    public List<CastingFavoriteResponseDTO> getFavoriteList(Long userId) {
        return castingCardRepository.findFavoritesByUserId(userId).stream()
                .map(this::toFavoriteResponseDTO)
                .toList();
    }

    private CastingFavoriteResponseDTO toFavoriteResponseDTO(CastingCard castingCard) {
        return CastingFavoriteResponseDTO.builder()
                .dailyRecordId(castingCard.getDailyRecordId())
                .genre(castingCard.getGenre())
                .roleName(castingCard.getRoleName())
                .highlight(castingCard.getHighlight())
                .oneLineComment(castingCard.getOneLineComment())
                .additionalMood(castingCard.getAdditionalMood())
                .isFavorite(castingCard.getIsFavorite())
                .generatedAt(castingCard.getGeneratedAt())
                .build();
    }

}
