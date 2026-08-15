package com.todayscasting.domain.record.controller;

import com.todayscasting.common.code.status.SuccessStatus;
import com.todayscasting.common.response.ApiResponse;
import com.todayscasting.domain.record.dto.request.DailyRecordCreateRequest;
import com.todayscasting.domain.record.dto.request.DailyRecordUpdateRequest;
import com.todayscasting.domain.record.dto.response.DailyRecordResponse;
import com.todayscasting.domain.record.dto.response.TodayStatusResponse;
import com.todayscasting.domain.record.service.DailyRecordService;
import com.todayscasting.domain.record.support.AuthenticatedUserResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "일일기록", description = "일일기록(daily record) 생성/조회/수정/삭제 API")
@RestController
@RequestMapping("/records")
@RequiredArgsConstructor
public class DailyRecordController {

    private final DailyRecordService dailyRecordService;
    private final AuthenticatedUserResolver authenticatedUserResolver;

    @Operation(summary = "일일기록 생성", description = "새로운 일일기록을 저장합니다. status(DRAFT/COMPLETED)를 선택적으로 받으며, 안 보내면 COMPLETED로 처리됩니다. 같은 날짜에 삭제된 기록이 있으면 복구해서 덮어쓰고, 이미 활성 기록이 있으면 409(DUPLICATE_RESOURCE)를 반환합니다.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<DailyRecordResponse> create(
            @Valid @RequestBody DailyRecordCreateRequest request,
            @AuthenticationPrincipal String email)
    {
        Long userId = authenticatedUserResolver.resolveUserId(email);
        DailyRecordResponse response = dailyRecordService.create(userId, request);
        return ApiResponse.of(SuccessStatus.CREATED, response);
    }

    @Operation(summary = "일일기록 수정", description = "recordId에 해당하는 기록을 수정합니다. status(DRAFT/COMPLETED)를 선택적으로 받으며, 안 보내면 COMPLETED로 처리됩니다. 본인 소유가 아니거나 삭제된 기록이면 404를 반환합니다.")
    @PutMapping("/{recordId}")
    public ApiResponse<DailyRecordResponse> update(
            @PathVariable Long recordId,
            @Valid @RequestBody DailyRecordUpdateRequest request,
            @AuthenticationPrincipal String email
    ) {
        Long userId = authenticatedUserResolver.resolveUserId(email);
        DailyRecordResponse response = dailyRecordService.update(userId, recordId, request);
        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "일일기록 삭제", description = "recordId에 해당하는 기록을 소프트 삭제합니다.")
    @DeleteMapping("/{recordId}")
    public ApiResponse<Void> delete(
            @PathVariable Long recordId,
            @AuthenticationPrincipal String email) {
        Long userId = authenticatedUserResolver.resolveUserId(email);
        dailyRecordService.delete(userId, recordId);
        return ApiResponse.onSuccess();
    }

    @Operation(summary = "날짜로 일일기록 조회", description = "특정 날짜의 기록을 조회합니다. 기록이 없으면 에러(404) 대신 200 응답이 나가고, 이때 result 필드는 생략됩니다(값이 없다는 뜻). draft 상태 기록도 그대로 반환되므로, 완료 여부는 result 유무가 아니라 status 값으로 판단해야 합니다.")
    @GetMapping
    public ApiResponse<DailyRecordResponse> getByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal String email
    ) {
        Long userId = authenticatedUserResolver.resolveUserId(email);
        DailyRecordResponse response = dailyRecordService.getByDate(userId, date);
        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "recordId로 일일기록 조회", description = "recordId로 기록 원문을 단건 조회합니다. 캘린더 미니카드 뒷면 등에서 사용합니다. status가 DRAFT인 기록은 조회되지 않으며(404 RESOURCE_NOT_FOUND), 완료(COMPLETED)된 기록만 조회 가능합니다.")
    @GetMapping("/{recordId}")
    public ApiResponse<DailyRecordResponse> getById(
            @PathVariable Long recordId,
            @AuthenticationPrincipal String email
    ) {
        Long userId = authenticatedUserResolver.resolveUserId(email);
        DailyRecordResponse response = dailyRecordService.getById(userId, recordId);
        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "태그로 일일기록 목록 조회", description = "mood/activityTag는 다중 선택(AND 매칭), moodTag는 단일 값으로 필터링합니다. 셋 다 비어있으면 400(MISSING_PARAMETER)을 반환합니다. status가 COMPLETED인 기록만 검색 대상이며, draft 상태 기록은 검색되지 않습니다.")
    @GetMapping("/tags")
    public ApiResponse<List<DailyRecordResponse>> getByTags(
            @RequestParam(required = false) List<String> mood,
            @RequestParam(required = false) String moodTag,
            @RequestParam(required = false) List<String> activityTag,
            @AuthenticationPrincipal String email
    ) {
        Long userId = authenticatedUserResolver.resolveUserId(email);
        List<DailyRecordResponse> response = dailyRecordService.getByTags(userId, mood, moodTag, activityTag);
        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "오늘의 기록 상태 조회", description = "캘린더 탭 '오늘의 기록 보기/작성하기' 버튼에서 사용. 오늘 기록 유무, 분석 진행상태, 캐스팅 카드 존재 여부를 종합해서 어느 화면으로 이동해야 하는지(screen)와 recordId를 함께 반환합니다.")
    @GetMapping("/today-status")
    public ApiResponse<TodayStatusResponse> getTodayStatus(
            @AuthenticationPrincipal String email
    ) {
        Long userId = authenticatedUserResolver.resolveUserId(email);
        TodayStatusResponse response = dailyRecordService.getTodayStatus(userId);
        return ApiResponse.onSuccess(response);
    }
}