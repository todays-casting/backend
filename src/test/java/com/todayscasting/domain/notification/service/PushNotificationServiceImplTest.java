package com.todayscasting.domain.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.todayscasting.domain.notification.dto.response.PushNotificationResponse;
import com.todayscasting.domain.notification.entity.UserSettings;
import com.todayscasting.domain.notification.repository.NotificationRepository;
import com.todayscasting.domain.notification.repository.UserFcmTokenRepository;
import com.todayscasting.domain.notification.repository.UserSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PushNotificationServiceImplTest {

    @Mock
    private UserFcmTokenRepository userFcmTokenRepository;

    @Mock
    private UserSettingsRepository userSettingsRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private ObjectProvider<FirebaseMessaging> firebaseMessagingProvider;

    @Mock
    private PlatformTransactionManager transactionManager;

    private PushNotificationServiceImpl pushNotificationService;

    @BeforeEach
    void setUp() {
        when(transactionManager.getTransaction(any(TransactionDefinition.class)))
                .thenReturn(new SimpleTransactionStatus());
        pushNotificationService = new PushNotificationServiceImpl(
                userFcmTokenRepository,
                userSettingsRepository,
                notificationRepository,
                firebaseMessagingProvider,
                transactionManager
        );
    }

    @Test
    void skipsCastingCardReadyNotificationWhenPushDisabled() {
        UserSettings settings = UserSettings.createDefault(1L);
        settings.update(false, false, null);
        when(userSettingsRepository.findByUserIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(settings));

        PushNotificationResponse response = pushNotificationService.sendCastingCardReady(1L, 10L);

        assertThat(response.successCount()).isZero();
        assertThat(response.failureCount()).isZero();
        verify(firebaseMessagingProvider, never()).getIfAvailable();
        verify(userFcmTokenRepository, never()).findByUserIdAndDeletedAtIsNull(1L);
    }
}
