package com.todayscasting.domain.casting.controller;

import com.todayscasting.common.response.ApiResponse;
import com.todayscasting.common.code.status.SuccessStatus;
import com.todayscasting.domain.casting.dto.request.CastingCardRequestDTO;
import com.todayscasting.domain.casting.dto.response.CastingCardResponseDTO;
import com.todayscasting.domain.casting.service.CastingCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/castings")
@RequiredArgsConstructor
public class CastingCardController {

    private final CastingCardService castingCardService;

    @PostMapping
    public ApiResponse<CastingCardResponseDTO> createCastingCard(
            @Valid @RequestBody CastingCardRequestDTO request
    ) {
        Long userId = 1L; // TODO: 로그인 기능 붙으면 인증 정보에서 꺼내는 걸로 교체 (DailyRecordController와 동일한 패턴)
        CastingCardResponseDTO result = castingCardService.createCastingCard(userId, request);
        return ApiResponse.of(SuccessStatus.CREATED, result);
    }

    @GetMapping("/{recordId}")
    public ApiResponse<CastingCardResponseDTO> getCastingCard(
            @PathVariable Long recordId
    ) {
        Long userId = 1L; // TODO: 로그인 기능 붙으면 인증 정보에서 꺼내는 걸로 교체
        CastingCardResponseDTO result = castingCardService.getCastingCard(userId, recordId);
        return ApiResponse.onSuccess(result);
    }

    @PatchMapping("/{recordId}/favorite")
    public ApiResponse<CastingCardResponseDTO> toggleFavorite(
            @PathVariable Long recordId
    ) {
        Long userId = 1L; // TODO: 로그인 기능 붙으면 인증 정보에서 꺼내는 걸로 교체
        CastingCardResponseDTO result = castingCardService.toggleFavorite(userId, recordId);
        return ApiResponse.onSuccess(result);
    }

}