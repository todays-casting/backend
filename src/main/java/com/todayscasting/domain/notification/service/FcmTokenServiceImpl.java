package com.todayscasting.domain.notification.service;

import com.todayscasting.domain.notification.dto.request.FcmTokenSaveRequest;
import com.todayscasting.domain.notification.entity.UserFcmToken;
import com.todayscasting.domain.notification.repository.UserFcmTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FcmTokenServiceImpl implements FcmTokenService {

    private final UserFcmTokenRepository userFcmTokenRepository;

    @Override
    @Transactional
    public void saveToken(Long userId, FcmTokenSaveRequest request) {
        UserFcmToken userFcmToken = userFcmTokenRepository.findByToken(request.token())
                .map(existingToken -> {
                    existingToken.updateOwner(userId);
                    existingToken.restore();
                    return existingToken;
                })
                .orElseGet(() -> UserFcmToken.create(userId, request.token()));

        userFcmTokenRepository.save(userFcmToken);
    }
}
