package com.todayscasting.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

// 캐스팅 카드 이미지 실시간 생성을 비동기로 처리하기 위한 설정. (이슈 #93)
// 카드 생성 API 응답 안에서 이미지 생성(최대 1~2분)까지 기다리면 프론트 요청이
// 타임아웃될 위험이 있어서, 이미지 생성은 백그라운드로 넘기고 카드 생성 응답은
// 즉시 반환하도록 분리한다.
@Configuration
@EnableAsync
public class AsyncConfig {
}