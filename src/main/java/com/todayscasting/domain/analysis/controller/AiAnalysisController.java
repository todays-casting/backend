package com.todayscasting.domain.analysis.controller;

import com.todayscasting.common.response.ApiResponse;
import com.todayscasting.common.code.status.SuccessStatus;
import com.todayscasting.domain.analysis.dto.request.AiAnalysisRequestDTO;
import com.todayscasting.domain.analysis.dto.response.AiAnalysisResponseDTO;
import com.todayscasting.domain.analysis.dto.response.AiAnalysisStatusResponseDTO;
import com.todayscasting.domain.analysis.service.AiAnalysisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/analyses")
@RequiredArgsConstructor
public class AiAnalysisController {

    private final AiAnalysisService aiAnalysisService;

    @PostMapping
    public ApiResponse<AiAnalysisResponseDTO> requestAnalysis(
            @Valid @RequestBody AiAnalysisRequestDTO request
    ) {
        AiAnalysisResponseDTO result = aiAnalysisService.requestAnalysis(request);
        return ApiResponse.of(SuccessStatus.CREATED, result);
    }

    @GetMapping("/{recordId}")
    public ApiResponse<AiAnalysisResponseDTO> getAnalysisResult(
            @PathVariable Long recordId
    ) {
        AiAnalysisResponseDTO result = aiAnalysisService.getAnalysisResult(recordId);
        return ApiResponse.onSuccess(result);
    }

    @GetMapping("/{recordId}/status")
    public ApiResponse<AiAnalysisStatusResponseDTO> getAnalysisStatus(
            @PathVariable Long recordId
    ) {
        AiAnalysisStatusResponseDTO result = aiAnalysisService.getAnalysisStatus(recordId);
        return ApiResponse.onSuccess(result);
    }

}