package com.todayscasting.domain.notification.service;

import com.todayscasting.domain.notification.dto.request.FcmTokenSaveRequest;
import com.todayscasting.domain.notification.repository.UserFcmTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FcmTokenServiceImplTest {

    @Mock
    private UserFcmTokenRepository userFcmTokenRepository;

    @InjectMocks
    private FcmTokenServiceImpl fcmTokenService;

    @Test
    void upsertsToken() {
        fcmTokenService.saveToken(1L, new FcmTokenSaveRequest("token-1"));

        verify(userFcmTokenRepository).upsertToken(1L, "token-1");
    }

    @Test
    void upsertHandlesExistingTokenOwnerUpdate() {
        fcmTokenService.saveToken(2L, new FcmTokenSaveRequest("token-1"));

        verify(userFcmTokenRepository).upsertToken(2L, "token-1");
    }
}
