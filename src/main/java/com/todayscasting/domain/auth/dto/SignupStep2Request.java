package com.todayscasting.domain.auth.dto;

import com.todayscasting.domain.user.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SignupStep2Request(
        @NotNull Long userId,
        @NotBlank String nickname,
        @NotNull Integer age,
        @NotNull User.Gender gender
) {}