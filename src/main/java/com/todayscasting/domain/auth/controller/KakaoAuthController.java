package com.todayscasting.domain.auth.controller;

import com.todayscasting.domain.auth.dto.TokenResponse;
import com.todayscasting.domain.auth.service.KakaoAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class KakaoAuthController {

    private final KakaoAuthService kakaoAuthService;

    @PostMapping("/kakao")
    public ResponseEntity<TokenResponse> kakaoLogin(@RequestParam String code) {
        return ResponseEntity.ok(kakaoAuthService.kakaoLogin(code));
    }
}