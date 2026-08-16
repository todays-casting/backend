package com.todayscasting.domain.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.todayscasting.domain.notification.dto.response.PushNotificationResponse;
import com.todayscasting.domain.notification.entity.Notification;
import com.todayscasting.domain.notification.entity.UserFcmToken;
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

import java.util.List;
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
    private FirebaseMessaging firebaseMessaging;

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
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void savesCastingCardReadyNotificationWhenPushSucceeds() throws Exception {
        when(userSettingsRepository.findByUserIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());
        when(userFcmTokenRepository.findByUserIdAndDeletedAtIsNull(1L))
                .thenReturn(List.of(UserFcmToken.create(1L, "token-1")));
        when(firebaseMessagingProvider.getIfAvailable()).thenReturn(firebaseMessaging);
        when(firebaseMessaging.send(any(Message.class))).thenReturn("message-id");

        PushNotificationResponse response = pushNotificationService.sendCastingCardReady(1L, 10L);

        assertThat(response.successCount()).isOne();
        assertThat(response.failureCount()).isZero();
        verify(notificationRepository).save(any(Notification.class));
    }
}
