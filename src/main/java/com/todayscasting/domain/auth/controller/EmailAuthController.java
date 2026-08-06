package com.todayscasting.domain.auth.controller;

import com.todayscasting.domain.auth.dto.EmailRequest;
import com.todayscasting.domain.auth.dto.EmailVerifyRequest;
import com.todayscasting.domain.auth.service.EmailAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/email")
@RequiredArgsConstructor
public class EmailAuthController {

    private final EmailAuthService emailAuthService;

    // 이메일 인증 코드 발송
    @PostMapping
    public ResponseEntity<Void> sendVerificationEmail(@RequestBody @Valid EmailRequest request) {
        emailAuthService.sendVerificationEmail(request.email());
        return ResponseEntity.ok().build();
    }

    // 이메일 인증 코드 확인
    @PostMapping("/verify")
    public ResponseEntity<Void> verifyCode(@RequestBody @Valid EmailVerifyRequest request) {
        emailAuthService.verifyCode(request.email(), request.code());
        return ResponseEntity.ok().build();
    }
}