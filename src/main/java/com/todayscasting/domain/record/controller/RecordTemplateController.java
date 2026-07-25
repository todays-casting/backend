package com.todayscasting.domain.record.controller;

import com.todayscasting.common.response.ApiResponse;
import com.todayscasting.domain.record.dto.response.RecordTemplateResponse;
import com.todayscasting.domain.record.service.RecordTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/records")
@RequiredArgsConstructor
public class RecordTemplateController {

    private final RecordTemplateService recordTemplateService;

    @GetMapping("/template")
    public ApiResponse<RecordTemplateResponse> getTodayTemplate() {
        RecordTemplateResponse response = recordTemplateService.getTodayTemplate();
        return ApiResponse.onSuccess(response);
    }
}
