package com.todayscasting.domain.auth.service;

import com.todayscasting.global.security.jwt.JwtProvider;
import com.todayscasting.domain.auth.code.AuthErrorStatus;
import com.todayscasting.domain.auth.dto.*;
import com.todayscasting.domain.auth.entity.Auth;
import com.todayscasting.domain.auth.repository.AuthRepository;
import com.todayscasting.domain.user.entity.User;
import com.todayscasting.domain.user.repository.UserRepository;
import com.todayscasting.common.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AuthRepository authRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public SignupStep1Response signUpStep1(SignupStep1Request request) {

        if (!request.password().equals(request.passwordConfirm())) {
            throw new GeneralException(AuthErrorStatus.PASSWORD_CONFIRM_MISMATCH);
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new GeneralException(AuthErrorStatus.EMAIL_ALREADY_EXISTS);
        }

        User user = userRepository.save(new User(request.email()));
        String passwordHash = passwordEncoder.encode(request.password());
        authRepository.save(new Auth(user, Auth.Provider.LOCAL, passwordHash));

        return new SignupStep1Response(user.getId());
    }

    @Transactional
    public TokenResponse signUpStep2(SignupStep2Request request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new GeneralException(AuthErrorStatus.USER_NOT_FOUND));

        user.updateProfile(request.nickname(), request.age(), request.gender());

        return new TokenResponse(jwtProvider.generateAccessToken(user.getEmail()));
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(request.email())
                .orElseThrow(() -> new GeneralException(AuthErrorStatus.USER_NOT_FOUND));

        Auth auth = authRepository.findByUserAndProvider(user, Auth.Provider.LOCAL)
                .orElseThrow(() -> new GeneralException(AuthErrorStatus.AUTH_NOT_FOUND));

        if (!passwordEncoder.matches(request.password(), auth.getPasswordHash())) {
            throw new GeneralException(AuthErrorStatus.INVALID_PASSWORD);
        }

        return new TokenResponse(jwtProvider.generateAccessToken(request.email()));
    }
}