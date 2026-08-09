package com.todayscasting.domain.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoTokenInfoResponse(
        Long id,
        @JsonProperty("app_id") Long appId,
        @JsonProperty("expires_in") Long expiresIn
) {}