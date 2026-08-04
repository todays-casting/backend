package com.todayscasting.domain.record.controller;

import com.todayscasting.common.code.status.SuccessStatus;
import com.todayscasting.common.response.ApiResponse;
import com.todayscasting.domain.record.dto.request.DailyRecordCreateRequest;
import com.todayscasting.domain.record.dto.request.DailyRecordUpdateRequest;
import com.todayscasting.domain.record.dto.response.DailyRecordResponse;
import com.todayscasting.domain.record.service.DailyRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/records")
@RequiredArgsConstructor
public class DailyRecordController {

    private final DailyRecordService dailyRecordService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<DailyRecordResponse> create(@Valid @RequestBody DailyRecordCreateRequest request) {
        Long userId = 1L; // TODO: 로그인 기능 붙으면 인증 정보에서 꺼내는 걸로 교체
        DailyRecordResponse response = dailyRecordService.create(userId, request);
        return ApiResponse.of(SuccessStatus.CREATED, response);
    }

    @PutMapping("/{recordId}")
    public ApiResponse<DailyRecordResponse> update(
            @PathVariable Long recordId,
            @Valid @RequestBody DailyRecordUpdateRequest request
    ) {
        Long userId = 1L; // TODO
        DailyRecordResponse response = dailyRecordService.update(userId, recordId, request);
        return ApiResponse.onSuccess(response);
    }

    @DeleteMapping("/{recordId}")
    public ApiResponse<Void> delete(@PathVariable Long recordId) {
        Long userId = 1L; // TODO
        dailyRecordService.delete(userId, recordId);
        return ApiResponse.onSuccess();
    }

    @GetMapping
    public ApiResponse<DailyRecordResponse> getByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        Long userId = 1L; // TODO
        DailyRecordResponse response = dailyRecordService.getByDate(userId, date);
        return ApiResponse.onSuccess(response);
    }

    @GetMapping("/{recordId}")
    public ApiResponse<DailyRecordResponse> getById(@PathVariable Long recordId) {
        Long userId = 1L; // TODO
        DailyRecordResponse response = dailyRecordService.getById(userId, recordId);
        return ApiResponse.onSuccess(response);
    }

    @GetMapping("/tags")
    public ApiResponse<List<DailyRecordResponse>> getByTags(
            @RequestParam(required = false) String mood,
            @RequestParam(required = false) String moodTag,
            @RequestParam(required = false) String activityTag
    ) {
        Long userId = 1L; // TODO
        List<DailyRecordResponse> response = dailyRecordService.getByTags(userId, mood, moodTag, activityTag);
        return ApiResponse.onSuccess(response);
    }
}