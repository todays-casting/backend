package com.todayscasting.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupStep1Request(
        @Email @NotBlank String email,
        @NotBlank @Size(min = 8) String password,
        @NotBlank String passwordConfirm
) {}