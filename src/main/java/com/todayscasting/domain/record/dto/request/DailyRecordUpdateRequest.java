package com.todayscasting.domain.record.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record DailyRecordUpdateRequest(
        @NotBlank String content,
        List<@Size(max = 10, message = "10자 이내로 입력해주세요") String> mood,
        @Size(max = 1, message = "분위기는 1개만 선택 가능합니다") List<@Size(max = 10, message = "10자 이내로 입력해주세요") String> moodTags,
        List<@Size(max = 10, message = "10자 이내로 입력해주세요") String> activityTags
) {}