package com.todayscasting.domain.auth.controller;

import com.todayscasting.domain.auth.dto.TokenResponse;
import com.todayscasting.domain.auth.service.KakaoAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class KakaoAuthController {

    private final KakaoAuthService kakaoAuthService;

    @PostMapping("/kakao")
    public ResponseEntity<TokenResponse> kakaoLogin(@RequestParam String code) {
        return ResponseEntity.ok(kakaoAuthService.kakaoLogin(code));
    }

    @GetMapping("/kakao/callback")
    public ResponseEntity<TokenResponse> kakaoCallback(@RequestParam String code) {
        return ResponseEntity.ok(kakaoAuthService.kakaoLogin(code));
    }
}