package com.todayscasting.domain.record.service;

import com.todayscasting.domain.record.dto.response.RecordTemplateResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RecordTemplateServiceImplTest {

    private final RecordTemplateServiceImpl recordTemplateService = new RecordTemplateServiceImpl();

    @Test // 질문, 가이드가 비어있진 않은지 테스트
    void returnsNonEmptyTemplate() {
        RecordTemplateResponse response = recordTemplateService.getTodayTemplate();

        assertThat(response.question()).isNotBlank();
        assertThat(response.guide()).isNotBlank();
    }

    @Test // 호출할 때마다 항상 같은 고정 질문이 나오는지 테스트
    void returnsSameFixedTemplateEveryTime() {
        RecordTemplateResponse first = recordTemplateService.getTodayTemplate();
        RecordTemplateResponse second = recordTemplateService.getTodayTemplate();

        assertThat(first).isEqualTo(second);
    }
}