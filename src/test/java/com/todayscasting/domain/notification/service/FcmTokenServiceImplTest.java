package com.todayscasting.domain.notification.service;

import com.todayscasting.domain.notification.dto.request.FcmTokenSaveRequest;
import com.todayscasting.domain.notification.entity.UserFcmToken;
import com.todayscasting.domain.notification.repository.UserFcmTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FcmTokenServiceImplTest {

    @Mock
    private UserFcmTokenRepository userFcmTokenRepository;

    @InjectMocks
    private FcmTokenServiceImpl fcmTokenService;

    @Test
    void savesNewToken() {
        when(userFcmTokenRepository.findByToken("token-1")).thenReturn(Optional.empty());

        fcmTokenService.saveToken(1L, new FcmTokenSaveRequest("token-1"));

        ArgumentCaptor<UserFcmToken> captor = ArgumentCaptor.forClass(UserFcmToken.class);
        verify(userFcmTokenRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(1L);
        assertThat(captor.getValue().getToken()).isEqualTo("token-1");
    }

    @Test
    void updatesOwnerWhenTokenAlreadyExists() {
        UserFcmToken existingToken = UserFcmToken.create(1L, "token-1");
        when(userFcmTokenRepository.findByToken("token-1")).thenReturn(Optional.of(existingToken));

        fcmTokenService.saveToken(2L, new FcmTokenSaveRequest("token-1"));

        verify(userFcmTokenRepository).save(existingToken);
        assertThat(existingToken.getUserId()).isEqualTo(2L);
    }
}
