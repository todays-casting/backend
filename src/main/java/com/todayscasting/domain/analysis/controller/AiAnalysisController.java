package com.todayscasting.domain.analysis.controller;

import com.todayscasting.common.response.ApiResponse;
import com.todayscasting.common.code.status.SuccessStatus;
import com.todayscasting.domain.analysis.dto.request.AiAnalysisRequestDTO;
import com.todayscasting.domain.analysis.dto.response.AiAnalysisResponseDTO;
import com.todayscasting.domain.analysis.dto.response.AiAnalysisStatusResponseDTO;
import com.todayscasting.domain.analysis.service.AiAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "AI 분석 API", description = "하루 기록을 바탕으로 OpenAI 분석을 요청하고 결과를 조회하는 API")
@RestController
@RequestMapping("/analyses")
@RequiredArgsConstructor
public class AiAnalysisController {

    private final AiAnalysisService aiAnalysisService;

    @Operation(
            summary = "AI 분석 요청",
            description = "dailyRecordId에 해당하는 하루 기록을 OpenAI로 분석합니다. " +
                    "이미 SUCCESS 상태의 분석이 있으면 400 에러가 발생하고, " +
                    "FAILED 상태(서버 에러이거나, 기록이 비어있거나 무의미해서 분석하지 못한 경우 포함)라면 " +
                    "같은 요청으로 다시 호출 시 자동으로 재시도됩니다."
    )
    @PostMapping
    public ApiResponse<AiAnalysisResponseDTO> requestAnalysis(
            @Valid @RequestBody AiAnalysisRequestDTO request
    ) {
        AiAnalysisResponseDTO result = aiAnalysisService.requestAnalysis(request);
        return ApiResponse.of(SuccessStatus.CREATED, result);
    }

    @Operation(
            summary = "AI 분석 결과 조회",
            description = "dailyRecordId(recordId)로 저장된 AI 분석 결과 전체(genre, roleName, highlight, oneLineComment, " +
                    "scenePhrase, commentPhrase, status, errorMessage 등)를 조회합니다."
    )
    @GetMapping("/{recordId}")
    public ApiResponse<AiAnalysisResponseDTO> getAnalysisResult(
            @PathVariable Long recordId
    ) {
        AiAnalysisResponseDTO result = aiAnalysisService.getAnalysisResult(recordId);
        return ApiResponse.onSuccess(result);
    }

    @Operation(
            summary = "AI 분석 상태 조회",
            description = "dailyRecordId(recordId)에 해당하는 분석의 진행 상태(PENDING, SUCCESS, FAILED)만 간단히 조회합니다."
    )
    @GetMapping("/{recordId}/status")
    public ApiResponse<AiAnalysisStatusResponseDTO> getAnalysisStatus(
            @PathVariable Long recordId
    ) {
        AiAnalysisStatusResponseDTO result = aiAnalysisService.getAnalysisStatus(recordId);
        return ApiResponse.onSuccess(result);
    }

}