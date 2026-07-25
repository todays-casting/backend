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
        CastingCardResponseDTO result = castingCardService.createCastingCard(request);
        return ApiResponse.of(SuccessStatus.CREATED, result);
    }

    @GetMapping("/{recordId}")
    public ApiResponse<CastingCardResponseDTO> getCastingCard(
            @PathVariable Long recordId
    ) {
        CastingCardResponseDTO result = castingCardService.getCastingCard(recordId);
        return ApiResponse.onSuccess(result);
    }

    @PatchMapping("/{recordId}/favorite")
    public ApiResponse<CastingCardResponseDTO> toggleFavorite(
            @PathVariable Long recordId
    ) {
        CastingCardResponseDTO result = castingCardService.toggleFavorite(recordId);
        return ApiResponse.onSuccess(result);
    }

}