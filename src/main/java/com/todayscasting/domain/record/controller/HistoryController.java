package com.todayscasting.domain.record.controller;

import com.todayscasting.common.response.ApiResponse;
import com.todayscasting.domain.record.dto.response.HistoryCardResponse;
import com.todayscasting.domain.record.service.HistoryService;
import com.todayscasting.domain.record.support.AuthenticatedUserResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    private final AuthenticatedUserResolver authenticatedUserResolver;

    @GetMapping
    public ApiResponse<List<HistoryCardResponse>> getHistory(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal String email
    ) {
        Long userId = authenticatedUserResolver.resolveUserId(email);
        List<HistoryCardResponse> response = historyService.getHistory(userId, startDate, endDate);
        return ApiResponse.onSuccess(response);
    }
}