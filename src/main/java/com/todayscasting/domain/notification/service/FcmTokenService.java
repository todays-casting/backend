package com.todayscasting.domain.notification.service;

import com.todayscasting.domain.notification.dto.request.FcmTokenSaveRequest;

public interface FcmTokenService {

    void saveToken(Long userId, FcmTokenSaveRequest request);
}
