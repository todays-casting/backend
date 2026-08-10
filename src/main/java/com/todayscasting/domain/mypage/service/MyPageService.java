package com.todayscasting.domain.mypage.service;

import com.todayscasting.domain.mypage.dto.response.MyPageResponse;

public interface MyPageService {

    MyPageResponse getMyPage(Long userId);
}
