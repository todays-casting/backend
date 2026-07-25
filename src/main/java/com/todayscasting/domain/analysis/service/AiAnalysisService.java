package com.todayscasting.domain.analysis.service;

import com.todayscasting.domain.analysis.dto.request.AiAnalysisRequestDTO;
import com.todayscasting.domain.analysis.dto.response.AiAnalysisResponseDTO;
import com.todayscasting.domain.analysis.dto.response.AiAnalysisStatusResponseDTO;

public interface AiAnalysisService {

    AiAnalysisResponseDTO requestAnalysis(AiAnalysisRequestDTO request);

    AiAnalysisResponseDTO getAnalysisResult(Long dailyRecordId);

    AiAnalysisStatusResponseDTO getAnalysisStatus(Long dailyRecordId);

}