package com.todayscasting.domain.casting.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CastingCardRequestDTO {

    @NotNull(message = "dailyRecordId는 필수입니다.")
    private Long dailyRecordId;

}