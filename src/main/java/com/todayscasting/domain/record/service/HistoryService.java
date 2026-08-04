package com.todayscasting.domain.record.service;

import com.todayscasting.domain.record.dto.response.HistoryCardResponse;

import java.time.LocalDate;
import java.util.List;

public interface HistoryService {

    List<HistoryCardResponse> getHistory(Long userId, LocalDate startDate, LocalDate endDate);
}
