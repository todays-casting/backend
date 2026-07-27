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
import com.todayscasting.domain.record.entity.DailyRecord;
import com.todayscasting.domain.record.repository.DailyRecordRepository;
import com.todayscasting.global.client.GeminiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiAnalysisServiceImpl implements AiAnalysisService {

    private final AiAnalysisLogRepository aiAnalysisLogRepository;
    private final DailyRecordRepository dailyRecordRepository;
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
        DailyRecord dailyRecord = dailyRecordRepository.findByIdAndDeletedAtIsNull(dailyRecordId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.RESOURCE_NOT_FOUND));

        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("당신은 사용자의 하루 기록을 분석해서, 재미있는 배역으로 캐스팅해주는 AI입니다.\n\n");
        promptBuilder.append("아래는 사용자가 오늘 작성한 하루 기록입니다:\n");
        promptBuilder.append("\"").append(dailyRecord.getContent()).append("\"\n\n");

        if (dailyRecord.getMood() != null && !dailyRecord.getMood().isEmpty()) {
            promptBuilder.append("오늘의 기분: ").append(String.join(", ", dailyRecord.getMood())).append("\n");
        }
        if (dailyRecord.getActivityTags() != null && !dailyRecord.getActivityTags().isEmpty()) {
            promptBuilder.append("오늘의 활동: ").append(String.join(", ", dailyRecord.getActivityTags())).append("\n");
        }

        promptBuilder.append("\n이 기록을 바탕으로, 아래 JSON 형식으로만 답변해주세요. ");
        promptBuilder.append("다른 설명이나 마크다운 없이 순수 JSON만 반환해야 합니다.\n\n");
        promptBuilder.append("{\n");
        promptBuilder.append("  \"title\": \"오늘 하루를 표현하는 캐스팅 카드 제목 (임팩트 있게)\",\n");
        promptBuilder.append("  \"subtitle\": \"제목을 보완하는 짧은 부제목\",\n");
        promptBuilder.append("  \"genre\": \"오늘 하루를 표현하는 장르 (예: 로맨스 드라마, 성장 영화 등)\",\n");
        promptBuilder.append("  \"roleName\": \"오늘 하루의 배역 이름 (짧고 인상적으로)\",\n");
        promptBuilder.append("  \"score\": \"0~100 사이의 점수\",\n");
        promptBuilder.append("  \"highlight\": \"오늘의 하이라이트 장면 한 줄\",\n");
        promptBuilder.append("  \"oneLineComment\": \"사용자에게 건네는 따뜻한 한 줄 코멘트\",\n");
        promptBuilder.append("  \"analysisSummary\": \"오늘 하루에 대한 짧은 분석 요약\"\n");
        promptBuilder.append("}");

        return promptBuilder.toString();
    }

    private String callAiServer(String prompt) {
        return geminiClient.generateContent(prompt);
    }

}