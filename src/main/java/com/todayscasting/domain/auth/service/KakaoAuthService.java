package com.todayscasting.domain.auth.service;

import com.todayscasting.domain.auth.client.KakaoClient;
import com.todayscasting.domain.auth.dto.KakaoUserResponse;
import com.todayscasting.domain.auth.dto.TokenResponse;
import com.todayscasting.domain.auth.entity.Auth;
import com.todayscasting.domain.auth.repository.AuthRepository;
import com.todayscasting.domain.user.entity.User;
import com.todayscasting.domain.user.repository.UserRepository;
import com.todayscasting.global.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class KakaoAuthService {

    private final KakaoClient kakaoClient;
    private final UserRepository userRepository;
    private final AuthRepository authRepository;
    private final JwtProvider jwtProvider;

    @Transactional
    public TokenResponse kakaoLogin(String code) {
        String kakaoAccessToken = kakaoClient.getToken(code).accessToken();
        KakaoUserResponse userInfo = kakaoClient.getUserInfo(kakaoAccessToken);

        if (userInfo.id() == null
                || userInfo.kakaoAccount() == null
                || userInfo.kakaoAccount().profile() == null
                || userInfo.kakaoAccount().profile().nickname() == null) {
            throw new RuntimeException("카카오 사용자 정보를 가져올 수 없습니다.");
        }

        String email = userInfo.kakaoAccount().email();
        String nickname = userInfo.kakaoAccount().profile().nickname();
        String providerId = String.valueOf(userInfo.id());

        Auth auth = authRepository.findByProviderAndProviderUserId(Auth.Provider.KAKAO, providerId)
                .orElseGet(() -> {
                    User newUser = userRepository.findByEmail(email)
                            .orElseGet(() -> userRepository.save(new User(email, nickname)));
                    return authRepository.save(new Auth(newUser, Auth.Provider.KAKAO, null, providerId));
                });

        User user = auth.getUser();

        return new TokenResponse(jwtProvider.generateAccessToken(user.getEmail()));

    }
}