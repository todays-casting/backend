package com.todayscasting.domain.auth.controller;

import com.todayscasting.domain.auth.dto.KakaoLoginRequest;
import com.todayscasting.domain.auth.dto.TokenResponse;
import com.todayscasting.domain.auth.service.KakaoAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class KakaoAuthController {

    private final KakaoAuthService kakaoAuthService;

    @PostMapping("/kakao")
    public ResponseEntity<TokenResponse> kakaoLogin(@RequestBody @Valid KakaoLoginRequest request) {
        return ResponseEntity.ok(kakaoAuthService.kakaoLogin(request.accessToken()));
    }
}