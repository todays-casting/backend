package com.todayscasting.domain.notification.entity;

public enum NotificationType {

    CASTING_CARD_READY(
            "오늘의 캐스팅",
            "오늘의 캐스팅 카드가 도착했어요."
    ),
    DAILY_RECORD_REMINDER(
            "오늘의 기록",
            "오늘 하루를 기록하고 캐스팅을 받아보세요."
    );

    private final String title;
    private final String body;

    NotificationType(String title, String body) {
        this.title = title;
        this.body = body;
    }

    public String title() {
        return title;
    }

    public String body() {
        return body;
    }
}
