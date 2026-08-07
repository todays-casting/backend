package com.todayscasting.domain.auth.controller;

import com.todayscasting.domain.auth.dto.*;
import com.todayscasting.domain.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "회원가입 1단계", description = "이메일, 비밀번호, 비밀번호 확인을 입력받아 계정을 생성합니다.")
    @PostMapping("/signup/step1")
    public ResponseEntity<SignupStep1Response> signUpStep1(
            @RequestBody @Valid SignupStep1Request request) {
        return ResponseEntity.ok(authService.signUpStep1(request));
    }

    @Operation(summary = "회원가입 2단계", description = "닉네임, 나이, 성별을 입력받아 프로필을 완성합니다.")
    @PostMapping("/signup/step2")
    public ResponseEntity<TokenResponse> signUpStep2(
            @RequestBody @Valid SignupStep2Request request) {
        return ResponseEntity.ok(authService.signUpStep2(request));
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다.")
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "비밀번호 찾기", description = "이메일로 임시 비밀번호를 발송합니다.")
    @PostMapping("/password/reset")
    public ResponseEntity<Void> resetPassword(@RequestBody @Valid PasswordResetRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "비밀번호 변경", description = "현재 비밀번호 확인 후 새 비밀번호로 변경합니다. JWT 인증 필요.")
    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal String email,
            @RequestBody @Valid PasswordChangeRequest request) {
        authService.changePassword(email, request);
        return ResponseEntity.ok().build();
    }
}