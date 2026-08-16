package com.todayscasting.domain.auth.controller;

import com.todayscasting.domain.auth.dto.*;
import com.todayscasting.domain.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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

    @Operation(summary = "비밀번호 재설정 요청", description = "이메일로 인증코드를 발송합니다.")
    @PostMapping("/password/reset/request")
    public ResponseEntity<Void> requestPasswordReset(
            @RequestBody @Valid PasswordResetRequestRequest request) {
        authService.requestPasswordReset(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "비밀번호 재설정 확인", description = "인증코드 검증 후 새 비밀번호로 변경합니다.")
    @PostMapping("/password/reset/confirm")
    public ResponseEntity<Void> confirmPasswordReset(
            @RequestBody @Valid PasswordResetConfirmRequest request) {
        authService.confirmPasswordReset(request);
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

    @Operation(summary = "로그아웃", description = "현재 토큰을 블랙리스트에 추가합니다. JWT 인증 필요.")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String token = resolveToken(request);
        authService.logout(token);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "회원 탈퇴", description = "계정을 삭제합니다. JWT 인증 필요.")
    @DeleteMapping("/me")
    public ResponseEntity<Void> withdraw(
            @AuthenticationPrincipal String email,
            HttpServletRequest request) {
        String token = resolveToken(request);
        authService.withdraw(email, token);
        return ResponseEntity.ok().build();
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}