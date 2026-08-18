package com.todayscasting.domain.casting.service;

import com.todayscasting.domain.analysis.entity.AiAnalysisLog;
import com.todayscasting.domain.analysis.repository.AiAnalysisLogRepository;
import com.todayscasting.domain.casting.dto.request.CastingCardRequestDTO;
import com.todayscasting.domain.casting.dto.response.CastingCardResponseDTO;
import com.todayscasting.domain.casting.entity.CastingCard;
import com.todayscasting.domain.casting.repository.CastingCardRepository;
import com.todayscasting.domain.notification.service.PushNotificationService;
import com.todayscasting.domain.record.entity.DailyRecord;
import com.todayscasting.domain.record.repository.DailyRecordRepository;
import com.todayscasting.domain.s3.service.S3Service;
import com.todayscasting.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CastingCardServiceImplTest {

    @Mock
    private CastingCardRepository castingCardRepository;

    @Mock
    private AiAnalysisLogRepository aiAnalysisLogRepository;

    @Mock
    private DailyRecordRepository dailyRecordRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PushNotificationService pushNotificationService;

    @Mock
    private S3Service s3Service;

    @Mock
    private CastingImageAsyncService castingImageAsyncService;

    private CastingCardServiceImpl castingCardService;

    @BeforeEach
    void setUp() {
        castingCardService = new CastingCardServiceImpl(
                castingCardRepository,
                aiAnalysisLogRepository,
                dailyRecordRepository,
                userRepository,
                pushNotificationService,
                s3Service,
                castingImageAsyncService
        );

        when(s3Service.createPublicGetUrl(any(String.class)))
                .thenAnswer(invocation -> "https://cdn.example.com/" + invocation.getArgument(0));
    }

    @Test
    void sendsNotificationWhenCastingCardCreated() {
        CastingCardRequestDTO request = request(10L);
        givenCastingCardCanBeCreated(1L, 10L);

        CastingCardResponseDTO response = castingCardService.createCastingCard(1L, request);

        assertThat(response.getDailyRecordId()).isEqualTo(10L);
        verify(pushNotificationService).sendCastingCardReady(1L, 10L);
        verify(castingImageAsyncService).generateAndAttachImage(100L, "Drama", null, "Highlight");
    }

    @Test
    void returnsCreatedCardEvenWhenNotificationFails() {
        CastingCardRequestDTO request = request(10L);
        givenCastingCardCanBeCreated(1L, 10L);
        doThrow(new IllegalStateException("fcm unavailable"))
                .when(pushNotificationService).sendCastingCardReady(1L, 10L);

        CastingCardResponseDTO response = castingCardService.createCastingCard(1L, request);

        assertThat(response.getDailyRecordId()).isEqualTo(10L);
        verify(pushNotificationService).sendCastingCardReady(1L, 10L);
    }

    private void givenCastingCardCanBeCreated(Long userId, Long dailyRecordId) {
        DailyRecord dailyRecord = DailyRecord.create(
                userId,
                LocalDate.of(2026, 8, 10),
                "content",
                List.of("GOOD"),
                List.of(),
                List.of(),
                DailyRecord.Status.COMPLETED
        );
        AiAnalysisLog analysisLog = AiAnalysisLog.builder()
                .dailyRecordId(dailyRecordId)
                .provider("OPENAI")
                .model("test")
                .prompt("prompt")
                .build();
        analysisLog.markSuccess("""
                {
                  "genre": "Drama",
                  "roleName": "Lead",
                  "highlight": "Highlight",
                  "oneLineComment": "Comment",
                  "additionalMood": ["CALM"]
                }
                """);

        when(dailyRecordRepository.findByIdAndUserIdAndDeletedAtIsNull(dailyRecordId, userId))
                .thenReturn(Optional.of(dailyRecord));
        when(castingCardRepository.findByDailyRecordId(dailyRecordId)).thenReturn(Optional.empty());
        when(aiAnalysisLogRepository.findByDailyRecordId(dailyRecordId)).thenReturn(Optional.of(analysisLog));
        when(castingCardRepository.save(any(CastingCard.class))).thenAnswer(invocation -> {
            CastingCard savedCard = invocation.getArgument(0);
            ReflectionTestUtils.setField(savedCard, "id", 100L);
            return savedCard;
        });
    }

    private CastingCardRequestDTO request(Long dailyRecordId) {
        CastingCardRequestDTO request = new CastingCardRequestDTO();
        ReflectionTestUtils.setField(request, "dailyRecordId", dailyRecordId);
        return request;
    }
}
