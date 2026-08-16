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
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AuthRepository authRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final TokenBlacklistService tokenBlacklistService;
    private final PasswordResetOtpService passwordResetOtpService;

    @Transactional
    public SignupStep1Response signUpStep1(SignupStep1Request request) {
        if (!request.password().equals(request.passwordConfirm())) {
            throw new GeneralException(AuthErrorStatus.PASSWORD_CONFIRM_MISMATCH);
        }
        if (userRepository.existsByEmailAndDeletedAtIsNull(request.email())) {
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
    public void requestPasswordReset(PasswordResetRequestRequest request) {
        Optional<User> userOpt = userRepository.findByEmailAndDeletedAtIsNull(request.email());
        if (userOpt.isEmpty()) return;

        Optional<Auth> authOpt = authRepository.findByUserAndProvider(userOpt.get(), Auth.Provider.LOCAL);
        if (authOpt.isEmpty()) return;

        String otp = passwordResetOtpService.generateAndStore(request.email());

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(request.email());
        message.setSubject("[투데이즈캐스팅] 비밀번호 재설정 인증코드");
        message.setText("인증코드: " + otp + "\n5분 내에 입력해주세요.");
        mailSender.send(message);
    }

    @Transactional
    public void confirmPasswordReset(PasswordResetConfirmRequest request) {
        if (!passwordResetOtpService.verifyAndInvalidate(request.email(), request.otp())) {
            throw new GeneralException(AuthErrorStatus.INVALID_OTP);
        }
        User user = userRepository.findByEmailAndDeletedAtIsNull(request.email())
                .orElseThrow(() -> new GeneralException(AuthErrorStatus.USER_NOT_FOUND));
        Auth auth = authRepository.findByUserAndProvider(user, Auth.Provider.LOCAL)
                .orElseThrow(() -> new GeneralException(AuthErrorStatus.AUTH_NOT_FOUND));

        auth.updatePasswordHash(passwordEncoder.encode(request.newPassword()));
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