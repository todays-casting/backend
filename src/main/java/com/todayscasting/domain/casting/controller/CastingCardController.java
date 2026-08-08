package com.todayscasting.domain.casting.controller;

import com.todayscasting.common.response.ApiResponse;
import com.todayscasting.common.code.status.SuccessStatus;
import com.todayscasting.domain.casting.dto.request.CastingCardRequestDTO;
import com.todayscasting.domain.casting.dto.response.CastingCardResponseDTO;
import com.todayscasting.domain.casting.dto.response.CastingFavoriteCountResponseDTO;
import com.todayscasting.domain.casting.dto.response.CastingFavoriteResponseDTO;
import com.todayscasting.domain.casting.service.CastingCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "캐스팅 결과 API", description = "AI 분석 결과를 바탕으로 캐스팅 카드를 생성/조회/즐겨찾기하는 API")
@RestController
@RequestMapping("/castings")
@RequiredArgsConstructor
public class CastingCardController {

    private final CastingCardService castingCardService;

    @Operation(
            summary = "캐스팅 카드 생성",
            description = "dailyRecordId에 해당하는 AI 분석 결과(SUCCESS 상태)를 바탕으로 캐스팅 카드를 생성합니다. " +
                    "본인이 작성한 하루 기록이 아니거나, 분석이 아직 성공하지 않았거나, 이미 카드가 존재하면 에러가 발생합니다."
    )
    @PostMapping
    public ApiResponse<CastingCardResponseDTO> createCastingCard(
            @Valid @RequestBody CastingCardRequestDTO request
    ) {
        Long userId = 1L; // TODO: 로그인 기능 붙으면 인증 정보에서 꺼내는 걸로 교체 (DailyRecordController와 동일한 패턴)
        CastingCardResponseDTO result = castingCardService.createCastingCard(userId, request);
        return ApiResponse.of(SuccessStatus.CREATED, result);
    }

    @Operation(
            summary = "캐스팅 카드 조회",
            description = "dailyRecordId(recordId)로 생성된 캐스팅 카드를 조회합니다. " +
                    "genre, roleName, highlight, oneLineComment, scenePhrase, commentPhrase, additionalMood, characterPhrase, isFavorite 등을 반환합니다."
    )
    @GetMapping("/{recordId}")
    public ApiResponse<CastingCardResponseDTO> getCastingCard(
            @PathVariable Long recordId
    ) {
        Long userId = 1L; // TODO: 로그인 기능 붙으면 인증 정보에서 꺼내는 걸로 교체
        CastingCardResponseDTO result = castingCardService.getCastingCard(userId, recordId);
        return ApiResponse.onSuccess(result);
    }

    @Operation(
            summary = "캐스팅 카드 즐겨찾기 토글",
            description = "dailyRecordId(recordId)에 해당하는 캐스팅 카드의 즐겨찾기(isFavorite) 상태를 켜고 끕니다."
    )
    @PatchMapping("/{recordId}/favorite")
    public ApiResponse<CastingCardResponseDTO> toggleFavorite(
            @PathVariable Long recordId
    ) {
        Long userId = 1L; // TODO: 로그인 기능 붙으면 인증 정보에서 꺼내는 걸로 교체
        CastingCardResponseDTO result = castingCardService.toggleFavorite(userId, recordId);
        return ApiResponse.onSuccess(result);
    }

    @Operation(
            summary = "즐겨찾기한 캐스팅 카드 개수 조회",
            description = "로그인한 사용자가 즐겨찾기(하트)한 캐스팅 카드의 총 개수를 조회합니다. " +
                    "마이페이지의 '찜한 카드' 개수 표시 등에 사용됩니다."
    )
    @GetMapping("/favorites/count")
    public ApiResponse<CastingFavoriteCountResponseDTO> getFavoriteCount() {
        Long userId = 1L; // TODO: 로그인 기능 붙으면 인증 정보에서 꺼내는 걸로 교체
        CastingFavoriteCountResponseDTO result = castingCardService.getFavoriteCount(userId);
        return ApiResponse.onSuccess(result);
    }

    @Operation(
            summary = "즐겨찾기한 캐스팅 카드 목록 조회",
            description = "로그인한 사용자가 즐겨찾기(하트)한 캐스팅 카드 전체 목록을 최신순으로 조회합니다. " +
                    "genre, roleName, highlight, oneLineComment, additionalMood, isFavorite, generatedAt을 반환합니다. " +
                    "마이페이지의 '저장한 카드' 화면 등에 사용됩니다."
    )
    @GetMapping("/favorites")
    public ApiResponse<List<CastingFavoriteResponseDTO>> getFavoriteList() {
        Long userId = 1L; // TODO: 로그인 기능 붙으면 인증 정보에서 꺼내는 걸로 교체
        List<CastingFavoriteResponseDTO> result = castingCardService.getFavoriteList(userId);
        return ApiResponse.onSuccess(result);
    }

}