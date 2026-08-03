package com.todayscasting.global.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OpenAiClient {

    private final OpenAiProperties openAiProperties;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.openai.com/v1")
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String generateContent(String prompt) {
        Map<String, Object> requestBody = Map.of(
                "model", openAiProperties.getModel(),
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                ),
                // OpenAI가 JSON 형식만 반환하도록 API 레벨에서 강제 (마크다운 코드블록 등 방지)
                "response_format", Map.of("type", "json_object")
        );

        String rawResponse = webClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + openAiProperties.getApiKey())
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(30))
                .block();

        return extractContent(rawResponse);
    }

    private String extractContent(String rawResponse) {
        if (rawResponse == null) {
            throw new IllegalStateException("OpenAI 응답이 비어있습니다.");
        }

        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            JsonNode choices = root.path("choices");

            if (!choices.isArray() || choices.isEmpty()) {
                throw new IllegalStateException("OpenAI 응답에 choices가 없습니다: " + rawResponse);
            }

            JsonNode contentNode = choices.get(0).path("message").path("content");

            if (contentNode.isMissingNode() || contentNode.isNull()) {
                throw new IllegalStateException("OpenAI 응답에 content가 없습니다: " + rawResponse);
            }

            return cleanJson(contentNode.asText());
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("OpenAI 응답 파싱에 실패했습니다: " + e.getMessage(), e);
        }
    }

    // response_format으로 JSON을 강제하긴 했지만, 혹시 모를 마크다운 코드블록에 대한 이중 방어
    // + OpenAI가 가끔 섞어 보내는 비표준 제어문자(예: \u0085 NEL) 제거
    private String cleanJson(String text) {
        String trimmed = text.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceAll("^```(json)?\\s*", "");
            trimmed = trimmed.replaceAll("```\\s*$", "");
        }
        trimmed = removeControlCharacters(trimmed);
        trimmed = removeLiteralUnicodeEscapeText(trimmed);
        return trimmed.trim();
    }

    // \t, \n, \r 등 정상적인 공백 제어문자는 남기고, 그 외 눈에 안 보이는 실제 제어문자만 제거
    private String removeControlCharacters(String text) {
        StringBuilder cleaned = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            boolean isAllowedWhitespace = c == '\t' || c == '\n' || c == '\r';
            boolean isControlChar = Character.isISOControl(c);
            if (isControlChar && !isAllowedWhitespace) {
                continue;
            }
            cleaned.append(c);
        }
        return cleaned.toString();
    }

    // OpenAI가 실제 제어문자가 아니라 "\u0085" 같은 이스케이프 표기를
    // 문자 그대로(백슬래시+u+4자리 hex) 출력하는 경우가 있어 이 패턴도 함께 제거
    private String removeLiteralUnicodeEscapeText(String text) {
        // C1 제어문자 범위(\u0080~\u009F)와 알려진 문제 문자(제로폭 공백, 라인/문단 구분자)를 텍스트 패턴으로 매칭
        return text.replaceAll("\\\\u00[89A-Fa-f][0-9A-Fa-f]", " ")
                .replaceAll("\\\\u200[Bb]", "")
                .replaceAll("\\\\u202[89]", " ");
    }

}