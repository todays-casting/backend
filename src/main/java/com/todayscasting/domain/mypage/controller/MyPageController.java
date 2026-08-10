package com.todayscasting.domain.mypage.controller;

import com.todayscasting.common.response.ApiResponse;
import com.todayscasting.domain.mypage.dto.response.MyPageResponse;
import com.todayscasting.domain.mypage.service.MyPageService;
import com.todayscasting.domain.record.support.AuthenticatedUserResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users/me/mypage")
@RequiredArgsConstructor
@Tag(name = "MyPage", description = "마이페이지 요약 API")
public class MyPageController {

    private final MyPageService myPageService;
    private final AuthenticatedUserResolver authenticatedUserResolver;

    @GetMapping
    @Operation(
            summary = "마이페이지 요약 조회",
            description = "로그인한 사용자의 기록 수, 연속 기록, 찜한 카드 수, 가입일수, 알림 설정을 조회합니다."
    )
    public ApiResponse<MyPageResponse> getMyPage(
            @AuthenticationPrincipal String email
    ) {
        Long userId = authenticatedUserResolver.resolveUserId(email);
        return ApiResponse.onSuccess(myPageService.getMyPage(userId));
    }
}
