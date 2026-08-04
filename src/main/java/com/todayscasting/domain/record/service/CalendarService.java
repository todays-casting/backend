package com.todayscasting.domain.record.service;

import com.todayscasting.domain.record.dto.response.CalendarResponse;

import java.time.YearMonth;
import java.util.List;

public interface CalendarService {

    List<CalendarResponse> getMonthlyCalendar(Long userId, YearMonth yearMonth);
}
