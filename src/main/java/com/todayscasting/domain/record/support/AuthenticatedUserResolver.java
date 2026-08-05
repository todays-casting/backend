package com.todayscasting.domain.record.support;

import com.todayscasting.common.exception.GeneralException;
import com.todayscasting.domain.auth.code.AuthErrorStatus;
import com.todayscasting.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// JWT에서 꺼낸 email로 실제 로그인한 유저의 userId를 조회해주는 헬퍼
// record 도메인 컨트롤러들(DailyRecord/Calendar/History)에서 공통으로 사용
// JwtAuthenticationFilter는 SecurityContext에 email만 담아두기 때문에,
// userId가 필요한 곳에서 이 클래스를 거쳐 email → userId로 변환함
@Component
@RequiredArgsConstructor
public class AuthenticatedUserResolver {

    private final UserRepository userRepository;

    // 컨트롤러에서 @AuthenticationPrincipal로 꺼낸 email을 넘기면 userId를 리턴
    public Long resolveUserId(String email) {
        return userRepository.findByEmailAndDeletedAtIsNull(email)
                // 정상적으로 로그인된 유저라면 항상 존재해야 하는 값이라,
                // 못 찾으면 토큰은 유효한데 유저 정보가 이상한 예외 상황 → 404로 처리
                .orElseThrow(() -> new GeneralException(AuthErrorStatus.USER_NOT_FOUND))
                .getId();
    }
}