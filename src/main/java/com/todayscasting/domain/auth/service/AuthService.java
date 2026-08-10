package com.todayscasting.domain.auth.service;

import com.todayscasting.global.security.jwt.JwtProvider;
import com.todayscasting.domain.auth.code.AuthErrorStatus;
import com.todayscasting.domain.auth.dto.*;
import com.todayscasting.domain.auth.entity.Auth;
import com.todayscasting.domain.auth.repository.AuthRepository;
import com.todayscasting.domain.user.entity.User;
import com.todayscasting.domain.user.repository.UserRepository;
import com.todayscasting.common.exception.GeneralException;
import com.todayscasting.global.security.jwt.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.mail.javamail.JavaMailSender;
import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AuthRepository authRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final TokenBlacklistService tokenBlacklistService;

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

    @Transactional
    public void resetPassword(PasswordResetRequest request) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(request.email())
                .orElseThrow(() -> new GeneralException(AuthErrorStatus.USER_NOT_FOUND));

        Auth auth = authRepository.findByUserAndProvider(user, Auth.Provider.LOCAL)
                .orElseThrow(() -> new GeneralException(AuthErrorStatus.AUTH_NOT_FOUND));

        String tempPassword = generateTempPassword();
        auth.updatePasswordHash(passwordEncoder.encode(tempPassword));

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(request.email());
        message.setSubject("[투데이즈캐스팅] 임시 비밀번호 안내");
        message.setText("임시 비밀번호: " + tempPassword + "\n로그인 후 비밀번호를 변경해주세요.");
        mailSender.send(message);
    }

    private String generateTempPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    @Transactional
    public void changePassword(String email, PasswordChangeRequest request) {
        if (!request.newPassword().equals(request.newPasswordConfirm())) {
            throw new GeneralException(AuthErrorStatus.PASSWORD_CONFIRM_MISMATCH);
        }

        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new GeneralException(AuthErrorStatus.USER_NOT_FOUND));

        Auth auth = authRepository.findByUserAndProvider(user, Auth.Provider.LOCAL)
                .orElseThrow(() -> new GeneralException(AuthErrorStatus.AUTH_NOT_FOUND));

        if (!passwordEncoder.matches(request.currentPassword(), auth.getPasswordHash())) {
            throw new GeneralException(AuthErrorStatus.INVALID_PASSWORD);
        }

        auth.updatePasswordHash(passwordEncoder.encode(request.newPassword()));
    }
    public void logout(String token) {
        long expiration = jwtProvider.getExpiration(token);
        if (expiration > 0) {
            tokenBlacklistService.addToBlacklist(token, expiration);
        }
    }

    @Transactional
    public void withdraw(String email, String token) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new GeneralException(AuthErrorStatus.USER_NOT_FOUND));
        user.withdraw();
        authRepository.findAllByUser(user).forEach(Auth::withdraw);
        logout(token);
    }
}