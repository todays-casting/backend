package com.todayscasting.domain.record.dto.response;

import java.time.LocalDate;

public record CalendarResponse(
        LocalDate recordDate,
        boolean hasRecord,
        boolean isFavorite
) {}