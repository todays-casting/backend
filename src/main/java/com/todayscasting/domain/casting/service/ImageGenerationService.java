package com.todayscasting.domain.casting.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.todayscasting.domain.user.entity.User;
import com.todayscasting.global.client.OpenAiProperties;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Base64;

// OpenAI Images API(gpt-image-2)의 edit 엔드포인트로 캐스팅 카드 배경 이미지를 실시간 생성한다. (이슈 #93)
//
// 세 가지를 함께 적용한 버전:
// 1) 모델을 gpt-image-1 -> gpt-image-2로 업그레이드 (지시사항 이행 개선 기대)
// 2) 참고 이미지(모험/청춘 기준 이미지) 2장을 매번 첨부 (images.edit)
// 3) 기준 이미지를 만들 때 실제로 성공했던 원본 프롬프트의 문장 패턴을 그대로 재사용
//    (뒷모습/살짝 옆모습, illustrated rather than lifelike, soft painterly strokes)
@Component
public class ImageGenerationService {

    private static final String MODEL = "gpt-image-2";
    private static final String SIZE = "1024x1536"; // 캐스팅 카드 세로형 비율

    // base64 인코딩된 이미지 응답은 WebClient 기본 버퍼 제한(256KB)을 훌쩍 넘기므로 10MB로 늘림
    private static final int MAX_RESPONSE_BUFFER_SIZE = 10 * 1024 * 1024;

    // 성별별 참고 이미지 파일명 (src/main/resources/reference-images/ 안에 위치)
    private static final String[] FEMALE_REFERENCE_IMAGES = {"모험_여성.png", "청춘_여성.png"};
    private static final String[] MALE_REFERENCE_IMAGES = {"모험_남성.png", "청춘_남성.png"};

    private final OpenAiProperties openAiProperties;
    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ImageGenerationService(OpenAiProperties openAiProperties) {
        this.openAiProperties = openAiProperties;

        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(MAX_RESPONSE_BUFFER_SIZE))
                .build();

        this.webClient = WebClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .exchangeStrategies(exchangeStrategies)
                .build();
    }

    /**
     * 프롬프트와 성별을 받아, 해당 성별의 참고 이미지를 첨부해서 이미지를 생성하고
     * 디코딩된 바이트 배열을 반환한다.
     */
    public byte[] generateImage(String prompt, User.Gender gender) {
        String[] referenceFilenames = gender == User.Gender.MALE ? MALE_REFERENCE_IMAGES : FEMALE_REFERENCE_IMAGES;

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("model", MODEL);
        builder.part("prompt", prompt);
        builder.part("size", SIZE);
        builder.part("quality", "medium");

        for (String filename : referenceFilenames) {
            byte[] referenceBytes = loadReferenceImage(filename);
            builder.part("image[]", new ByteArrayResource(referenceBytes) {
                @Override
                public String getFilename() {
                    return filename;
                }
            }).filename(filename);
        }

        String rawResponse = webClient.post()
                .uri("/images/edits")
                .header("Authorization", "Bearer " + openAiProperties.getApiKey())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(120)) // 고해상도 이미지 생성에 60초 넘게 걸리는 경우가 있어 넉넉히 설정
                .block();

        return extractImageBytes(rawResponse);
    }

    private byte[] loadReferenceImage(String filename) {
        ClassPathResource resource = new ClassPathResource("reference-images/" + filename);
        try (InputStream inputStream = resource.getInputStream()) {
            return inputStream.readAllBytes();
        } catch (IOException e) {
            throw new IllegalStateException("참고 이미지를 읽을 수 없습니다: " + filename, e);
        }
    }

    private byte[] extractImageBytes(String rawResponse) {
        if (rawResponse == null) {
            throw new IllegalStateException("OpenAI 이미지 생성 응답이 비어있습니다.");
        }

        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            JsonNode data = root.path("data");

            if (!data.isArray() || data.isEmpty()) {
                throw new IllegalStateException("OpenAI 이미지 생성 응답에 data가 없습니다: " + rawResponse);
            }

            JsonNode base64Node = data.get(0).path("b64_json");

            if (base64Node.isMissingNode() || base64Node.isNull()) {
                throw new IllegalStateException("OpenAI 이미지 생성 응답에 b64_json이 없습니다: " + rawResponse);
            }

            return Base64.getDecoder().decode(base64Node.asText());
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("OpenAI 이미지 생성 응답 파싱에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * genre/highlight/성별을 바탕으로, 기준 이미지(모험/청춘)를 만들 때 실제로 성공했던
     * 프롬프트의 문장 패턴을 그대로 재사용해서 프롬프트를 구성한다.
     * 참고 이미지도 함께 첨부되므로, 첫 문장에서 참고 이미지 우선 참고를 명시한다.
     */
    public String buildPrompt(String genre, User.Gender gender, String highlight) {
        String genderNoun = gender == User.Gender.MALE ? "young man" : "young woman";

        StringBuilder prompt = new StringBuilder();
        prompt.append("Study the attached reference images closely and match ONLY their art style — brushwork, ")
                .append("color rendering quality, and overall illustration technique. Do NOT copy their specific ")
                .append("location, composition, or time of day/lighting — those should come entirely from the ")
                .append("scene described below instead. ");

        prompt.append("A semi-realistic digital painting illustration, portrait orientation, not photorealistic — ")
                .append("stylized character art with painterly brushstrokes rather than photographic realism. ");

        prompt.append("Illustrate a ").append(genderNoun).append(" ");
        if (isUsableHighlight(highlight)) {
            prompt.append("in a scene inspired by this moment from their day: \"").append(highlight).append("\", ")
                    .append("set in a location and setting that actually fits this moment (do not default to an ")
                    .append("outdoor mountain/hillside overlook unless the moment described is actually about ")
                    .append("that kind of place), ");
        } else {
            prompt.append("in a fitting everyday moment, ");
        }
        prompt.append("with an overall mood and genre of \"").append(genre)
                .append("\" (weight the first word of this the most, treat the rest as a secondary flavor). ");

        prompt.append("The character should be seen mostly from behind and slightly from the side, ")
                .append("illustrated rather than lifelike, rendered with soft painterly strokes rather than ")
                .append("photographic detail — facial features should not be clearly visible. ");

        prompt.append("Loose, modest clothing with no tight or revealing silhouette. ");

        prompt.append("Vary the time of day and lighting naturally based on what fits the scene and mood — ")
                .append("across many images, aim for a healthy mix where roughly one in three lean toward ")
                .append("bright daytime light, with the rest spread across sunset, overcast, night, and warm ")
                .append("indoor lighting as fits the scene. Don't force any one time of day onto every scene; ")
                .append("let the specific moment described decide. Keep a dark-mode-app-friendly color palette ")
                .append("and avoid harsh bright white daylight tones.");

        return prompt.toString();
    }

    // "-"(미작성 기록 고정값)나 빈 문자열처럼 실질적인 내용이 없는 경우는 디테일로 쓰지 않는다.
    private boolean isUsableHighlight(String highlight) {
        return highlight != null && !highlight.isBlank() && !"-".equals(highlight.trim());
    }

}