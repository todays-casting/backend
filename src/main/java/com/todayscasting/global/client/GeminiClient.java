package com.todayscasting.global.client;

import com.todayscasting.common.code.status.ErrorStatus;
import com.todayscasting.common.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GeminiClient {

    private final GeminiProperties geminiProperties;

    private WebClient webClient() {
        return WebClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta/models/"
                        + geminiProperties.getModel() + ":generateContent")
                .build();
    }

    public String generateContent(String prompt) {

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                )
        );

        Map<String, Object> response = webClient()
                .post()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("key", geminiProperties.getApiKey())
                        .build())
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(30))
                .block();

        String rawText = extractText(response);
        return cleanJson(rawText);
    }

    @SuppressWarnings("unchecked")
    private String extractText(Map<String, Object> response) {
        if (response == null) {
            throw new GeneralException(ErrorStatus.INTERNAL_SERVER_ERROR);
        }

        List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
        if (candidates == null || candidates.isEmpty()) {
            throw new GeneralException(ErrorStatus.INTERNAL_SERVER_ERROR);
        }

        Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
        if (content == null) {
            throw new GeneralException(ErrorStatus.INTERNAL_SERVER_ERROR);
        }

        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
        if (parts == null || parts.isEmpty()) {
            throw new GeneralException(ErrorStatus.INTERNAL_SERVER_ERROR);
        }

        Object text = parts.get(0).get("text");
        if (text == null) {
            throw new GeneralException(ErrorStatus.INTERNAL_SERVER_ERROR);
        }

        return (String) text;
    }

    // Gemini 응답이 마크다운 코드블록(```json ... ```)으로 감싸져 오는 경우,
    // 그 껍데기를 제거하고 순수 JSON 문자열만 남긴다.
    private String cleanJson(String text) {
        String trimmed = text.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceFirst("^```(json)?", "");
            trimmed = trimmed.replaceFirst("```$", "");
        }
        return trimmed.trim();
    }

}