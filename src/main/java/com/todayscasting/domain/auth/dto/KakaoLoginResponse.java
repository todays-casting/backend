package com.todayscasting.domain.auth.dto;

public record KakaoLoginResponse(String accessToken, boolean isNewUser, Long userId) {}