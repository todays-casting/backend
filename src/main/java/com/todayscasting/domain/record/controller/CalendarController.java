package com.todayscasting.domain.record.controller;

import com.todayscasting.common.response.ApiResponse;
import com.todayscasting.domain.record.dto.response.CalendarResponse;
import com.todayscasting.domain.record.service.CalendarService;
import com.todayscasting.domain.record.support.AuthenticatedUserResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/records/history")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;
    private final AuthenticatedUserResolver authenticatedUserResolver;

    @GetMapping("/{yearMonth}")
    public ApiResponse<List<CalendarResponse>> getMonthlyCalendar(
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth,
            @AuthenticationPrincipal String email
    ) {
        Long userId = authenticatedUserResolver.resolveUserId(email);
        List<CalendarResponse> response = calendarService.getMonthlyCalendar(userId, yearMonth);
        return ApiResponse.onSuccess(response);
    }
}