package com.todayscasting.domain.record.service;

import com.todayscasting.domain.record.dto.response.RecordTemplateResponse;
import org.springframework.stereotype.Service;

@Service
public class RecordTemplateServiceImpl implements RecordTemplateService {

    private static final RecordTemplateResponse TEMPLATE =
            new RecordTemplateResponse("오늘은 어떤 이야기가 당신의 하루를 채웠나요?", "오늘 하루의 생각, 순간, 감정을 자유롭게 기록해보세요.");

    @Override
    public RecordTemplateResponse getTodayTemplate() {
        return TEMPLATE;
    }
}