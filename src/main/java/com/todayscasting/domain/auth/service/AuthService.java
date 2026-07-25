package com.todayscasting.domain.auth.service;

import com.todayscasting.global.security.jwt.JwtProvider;
import com.todayscasting.domain.auth.code.AuthErrorStatus;
import com.todayscasting.domain.auth.dto.LoginRequest;
import com.todayscasting.domain.auth.dto.SignUpRequest;
import com.todayscasting.domain.auth.dto.TokenResponse;
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
    public TokenResponse signUp(SignUpRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new GeneralException(AuthErrorStatus.EMAIL_ALREADY_EXISTS);
        }

        User user = new User(request.email(), request.nickname());
        userRepository.save(user);

        String passwordHash = passwordEncoder.encode(request.password());
        Auth auth = new Auth(user, Auth.Provider.LOCAL, passwordHash);
        authRepository.save(auth);

        String token = jwtProvider.generateAccessToken(request.email());
        return new TokenResponse(token);
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

        String token = jwtProvider.generateAccessToken(request.email());
        return new TokenResponse(token);
    }
}