package com.todayscasting.domain.record.controller;

import com.todayscasting.common.response.ApiResponse;
import com.todayscasting.domain.record.dto.response.HistoryCardResponse;
import com.todayscasting.domain.record.service.HistoryService;
import com.todayscasting.domain.record.support.AuthenticatedUserResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "히스토리", description = "히스토리 탭 날짜범위 카드 캐러셀 조회 API")
@RestController
@RequestMapping("/records/history")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;
    private final AuthenticatedUserResolver authenticatedUserResolver;

    @Operation(summary = "날짜범위 히스토리 카드 조회", description = "startDate~endDate 범위의 완료된 기록을 카드 형태로 반환합니다. 캐스팅 결과가 아직 없는 날도 포함하며, 이 경우 hasCastingCard=false이고 캐스팅 관련 필드는 null로 반환합니다.")
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
