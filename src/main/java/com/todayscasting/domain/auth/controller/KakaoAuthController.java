package com.todayscasting.domain.auth.controller;

import com.todayscasting.domain.auth.dto.KakaoLoginRequest;
import com.todayscasting.domain.auth.dto.TokenResponse;
import com.todayscasting.domain.auth.service.KakaoAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class KakaoAuthController {

    private final KakaoAuthService kakaoAuthService;

    @Operation(summary = "카카오 로그인", description = "카카오 SDK로 발급받은 액세스 토큰으로 로그인합니다.")
    @PostMapping("/kakao")
    public ResponseEntity<TokenResponse> kakaoLogin(@RequestBody @Valid KakaoLoginRequest request) {
        return ResponseEntity.ok(kakaoAuthService.kakaoLogin(request.accessToken()));
    }
}