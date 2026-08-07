package com.todayscasting.domain.auth.controller;

import com.todayscasting.domain.auth.dto.*;
import com.todayscasting.domain.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup/step1")
    public ResponseEntity<SignupStep1Response> signUpStep1(
            @RequestBody @Valid SignupStep1Request request) {
        return ResponseEntity.ok(authService.signUpStep1(request));
    }

    @PostMapping("/signup/step2")
    public ResponseEntity<TokenResponse> signUpStep2(
            @RequestBody @Valid SignupStep2Request request) {
        return ResponseEntity.ok(authService.signUpStep2(request));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}