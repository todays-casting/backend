package com.todayscasting.domain.analysis.service;

import com.todayscasting.domain.analysis.dto.request.AiAnalysisRequestDTO;
import com.todayscasting.domain.analysis.dto.response.AiAnalysisResponseDTO;
import com.todayscasting.domain.analysis.dto.response.AiAnalysisStatusResponseDTO;

public interface AiAnalysisService {

    AiAnalysisResponseDTO requestAnalysis(Long userId, AiAnalysisRequestDTO request);

    AiAnalysisResponseDTO getAnalysisResult(Long userId, Long dailyRecordId);

    AiAnalysisStatusResponseDTO getAnalysisStatus(Long userId, Long dailyRecordId);

}