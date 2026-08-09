package com.todayscasting.domain.record.controller;

import com.todayscasting.common.response.ApiResponse;
import com.todayscasting.domain.record.dto.response.RecordTemplateResponse;
import com.todayscasting.domain.record.service.RecordTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "기록템플릿", description = "오늘의 질문/가이드 문구 조회 API")
@RestController
@RequestMapping("/records")
@RequiredArgsConstructor
public class RecordTemplateController {

    private final RecordTemplateService recordTemplateService;

    @Operation(summary = "오늘의 기록 템플릿 조회", description = "일기 작성 화면에 보여줄 고정된 질문/가이드 문구를 반환합니다. DB 없이 하드코딩된 값입니다.")
    @GetMapping("/template")
    public ApiResponse<RecordTemplateResponse> getTodayTemplate() {
        RecordTemplateResponse response = recordTemplateService.getTodayTemplate();
        return ApiResponse.onSuccess(response);
    }
}
