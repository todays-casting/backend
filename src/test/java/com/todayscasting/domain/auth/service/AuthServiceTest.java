package com.todayscasting.domain.auth.service;

import com.todayscasting.common.exception.GeneralException;
import com.todayscasting.domain.auth.code.AuthErrorStatus;
import com.todayscasting.domain.auth.dto.SignupStep1Request;
import com.todayscasting.domain.auth.entity.WithdrawnEmail;
import com.todayscasting.domain.auth.repository.AuthRepository;
import com.todayscasting.domain.auth.repository.WithdrawnEmailRepository;
import com.todayscasting.domain.user.entity.User;
import com.todayscasting.domain.user.repository.UserRepository;
import com.todayscasting.global.security.jwt.JwtProvider;
import com.todayscasting.global.security.jwt.TokenBlacklistService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthRepository authRepository;

    @Mock
    private WithdrawnEmailRepository withdrawnEmailRepository;

    @Mock
    private EmailHashService emailHashService;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private PasswordResetOtpService passwordResetOtpService;

    @InjectMocks
    private AuthService authService;

    @Test
    void blocksSignupWhenEmailWasWithdrawn() {
        SignupStep1Request request = new SignupStep1Request(
                "old@example.com",
                "password123!",
                "password123!"
        );
        when(emailHashService.hash("old@example.com")).thenReturn("email-hash");
        when(withdrawnEmailRepository.existsByEmailHash("email-hash")).thenReturn(true);

        assertThatThrownBy(() -> authService.signUpStep1(request))
                .isInstanceOf(GeneralException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorStatus.WITHDRAWN_EMAIL_CANNOT_SIGNUP);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void storesWithdrawnEmailHashBeforeWithdrawingUser() {
        User user = new User("old@example.com");
        when(userRepository.findByEmailAndDeletedAtIsNull("old@example.com")).thenReturn(Optional.of(user));
        when(emailHashService.hash("old@example.com")).thenReturn("email-hash");
        when(withdrawnEmailRepository.existsByEmailHash("email-hash")).thenReturn(false);
        when(authRepository.findAllByUser(user)).thenReturn(List.of());

        authService.withdraw("old@example.com", "access-token");

        verify(withdrawnEmailRepository).save(any(WithdrawnEmail.class));
    }
}
