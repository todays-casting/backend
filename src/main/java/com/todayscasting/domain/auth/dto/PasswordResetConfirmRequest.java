package com.todayscasting.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordResetConfirmRequest(
        @NotBlank @Email String email,
        @NotBlank String otp,
        @NotBlank @Size(min = 8) String newPassword
) {}