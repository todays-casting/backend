package com.todayscasting.domain.auth.client;

import com.todayscasting.domain.auth.dto.KakaoTokenInfoResponse;
import com.todayscasting.domain.auth.dto.KakaoUserResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class KakaoClient {

    private final RestClient restClient;

    @Value("${kakao.app-id}")
    private Long appId;

    public KakaoClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(5000);
        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .build();
    }

    public void validateToken(String accessToken) {
        KakaoTokenInfoResponse response = restClient.get()
                .uri("https://kapi.kakao.com/v1/user/access_token_info")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(KakaoTokenInfoResponse.class);

        if (response == null) {
            throw new RuntimeException("카카오 토큰 정보를 가져올 수 없습니다.");
        }
        if (!appId.equals(response.appId())) {
            throw new RuntimeException("유효하지 않은 카카오 토큰입니다.");
        }
    }

    public KakaoUserResponse getUserInfo(String accessToken) {
        KakaoUserResponse response = restClient.get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(KakaoUserResponse.class);

        if (response == null) {
            throw new RuntimeException("카카오 사용자 정보를 가져올 수 없습니다.");
        }
        return response;
    }
}