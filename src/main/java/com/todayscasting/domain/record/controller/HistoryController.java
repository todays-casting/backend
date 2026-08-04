package com.todayscasting.domain.record.controller;

import com.todayscasting.common.response.ApiResponse;
import com.todayscasting.domain.record.dto.response.HistoryCardResponse;
import com.todayscasting.domain.record.service.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/records/history")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;

    @GetMapping
    public ApiResponse<List<HistoryCardResponse>> getHistory(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        Long userId = 1L; // TODO: 로그인 기능 붙으면 인증 정보에서 꺼내는 걸로 교체
        List<HistoryCardResponse> response = historyService.getHistory(userId, startDate, endDate);
        return ApiResponse.onSuccess(response);
    }
}