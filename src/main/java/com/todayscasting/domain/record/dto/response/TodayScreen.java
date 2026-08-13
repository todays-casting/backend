package com.todayscasting.domain.record.dto.response;

public enum TodayScreen {
    INCOMPLETE, // 기록 없음 또는 status:DRAFT
    WAITING,    // COMPLETED + (분석 미완료 또는 SUCCESS인데 카드 없음)
    RESULT,     // COMPLETED + SUCCESS + 카드 존재
    FAILED      // 분석 FAILED
}