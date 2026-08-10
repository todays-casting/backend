package com.todayscasting.domain.record.controller;

import com.todayscasting.common.response.ApiResponse;
import com.todayscasting.domain.record.dto.response.CalendarResponse;
import com.todayscasting.domain.record.service.CalendarService;
import com.todayscasting.domain.record.support.AuthenticatedUserResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.List;

@Tag(name = "캘린더", description = "달력 탭 월별 마커 조회 API")
@RestController
@RequestMapping("/records/history")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;
    private final AuthenticatedUserResolver authenticatedUserResolver;

    @Operation(summary = "월별 캘린더 마커 조회", description = "yearMonth(yyyy-MM)에 해당하는 달의 날짜별 기록 유무/즐겨찾기 여부를 반환합니다. 날짜 클릭 시 recordId는 GET /records?date=로 별도 조회합니다.")
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