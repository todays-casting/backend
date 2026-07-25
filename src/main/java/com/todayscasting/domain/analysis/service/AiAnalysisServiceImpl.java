package com.todayscasting.domain.analysis.service;

import com.todayscasting.common.code.status.ErrorStatus;
import com.todayscasting.common.exception.GeneralException;
import com.todayscasting.domain.analysis.converter.AiAnalysisConverter;
import com.todayscasting.domain.analysis.dto.request.AiAnalysisRequestDTO;
import com.todayscasting.domain.analysis.dto.response.AiAnalysisResponseDTO;
import com.todayscasting.domain.analysis.dto.response.AiAnalysisStatusResponseDTO;
import com.todayscasting.domain.analysis.entity.AiAnalysisLog;
import com.todayscasting.domain.analysis.entity.AnalysisStatus;
import com.todayscasting.domain.analysis.repository.AiAnalysisLogRepository;
import com.todayscasting.global.client.GeminiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiAnalysisServiceImpl implements AiAnalysisService {

    private final AiAnalysisLogRepository aiAnalysisLogRepository;
    private final GeminiClient geminiClient;

    @Override
    public AiAnalysisResponseDTO requestAnalysis(AiAnalysisRequestDTO request) {

        AiAnalysisLog savedLog = savePendingLog(request);

        String rawResponse = null;
        try {
            rawResponse = callAiServer(savedLog.getPrompt());
        } catch (Exception e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            markFailed(savedLog.getId(), errorMessage);
        }

        if (rawResponse != null) {
            markSuccess(savedLog.getId(), rawResponse);
        }

        AiAnalysisLog finalLog = findByDailyRecordIdOrThrow(request.getDailyRecordId());
        return AiAnalysisConverter.toResponseDTO(finalLog);
    }

    public AiAnalysisLog savePendingLog(AiAnalysisRequestDTO request) {
        AiAnalysisLog existingLog = aiAnalysisLogRepository
                .findByDailyRecordId(request.getDailyRecordId())
                .orElse(null);

        if (existingLog != null) {
            if (existingLog.getStatus() == AnalysisStatus.FAILED) {
                existingLog.retry(buildPrompt(request.getDailyRecordId()));
                return aiAnalysisLogRepository.save(existingLog);
            }
            throw new GeneralException(ErrorStatus.INVALID_REQUEST);
        }

        AiAnalysisLog aiAnalysisLog = AiAnalysisLog.builder()
                .dailyRecordId(request.getDailyRecordId())
                .provider("GEMINI")
                .model("gemini-3.5-flash")
                .prompt(buildPrompt(request.getDailyRecordId()))
                .build();

        try {
            return aiAnalysisLogRepository.save(aiAnalysisLog);
        } catch (DataIntegrityViolationException e) {
            throw new GeneralException(ErrorStatus.INVALID_REQUEST);
        }
    }

    public void markSuccess(Long id, String rawResponse) {
        AiAnalysisLog log = aiAnalysisLogRepository.findById(id)
                .orElseThrow(() -> new GeneralException(ErrorStatus.RESOURCE_NOT_FOUND));
        log.markSuccess(rawResponse);
        aiAnalysisLogRepository.save(log);
    }

    public void markFailed(Long id, String errorMessage) {
        AiAnalysisLog log = aiAnalysisLogRepository.findById(id)
                .orElseThrow(() -> new GeneralException(ErrorStatus.RESOURCE_NOT_FOUND));
        log.markFailed(errorMessage);
        aiAnalysisLogRepository.save(log);
    }

    @Override
    public AiAnalysisResponseDTO getAnalysisResult(Long dailyRecordId) {
        AiAnalysisLog aiAnalysisLog = findByDailyRecordIdOrThrow(dailyRecordId);
        return AiAnalysisConverter.toResponseDTO(aiAnalysisLog);
    }

    @Override
    public AiAnalysisStatusResponseDTO getAnalysisStatus(Long dailyRecordId) {
        AiAnalysisLog aiAnalysisLog = findByDailyRecordIdOrThrow(dailyRecordId);
        return AiAnalysisConverter.toStatusResponseDTO(aiAnalysisLog);
    }

    private AiAnalysisLog findByDailyRecordIdOrThrow(Long dailyRecordId) {
        return aiAnalysisLogRepository.findByDailyRecordId(dailyRecordId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.RESOURCE_NOT_FOUND));
    }

    private String buildPrompt(Long dailyRecordId) {
        // TODO: daily_records 테이블에서 content 조회해서 실제 프롬프트로 조립 (다음 단계에서 진행)
        return "daily_record_id " + dailyRecordId + "에 대한 분석 요청입니다. "
                + "JSON 형식으로만 답변해주세요.";
    }

    private String callAiServer(String prompt) {
        return geminiClient.generateContent(prompt);
    }

}