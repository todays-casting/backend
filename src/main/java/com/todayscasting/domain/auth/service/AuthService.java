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
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AuthRepository authRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final EmailAuthService emailAuthService;

    @Transactional
    public SignupStep1Response signUpStep1(SignupStep1Request request) {
        if (!emailAuthService.isVerified(request.email())) {
            throw new GeneralException(AuthErrorStatus.EMAIL_NOT_VERIFIED);
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

        String email = user.getEmail();
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        emailAuthService.deleteVerified(email);
                    }
                }
        );

        return new TokenResponse(jwtProvider.generateAccessToken(email));
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