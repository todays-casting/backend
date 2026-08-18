package com.todayscasting.domain.casting.service;

import com.todayscasting.domain.casting.entity.CastingCard;
import com.todayscasting.domain.casting.repository.CastingCardRepository;
import com.todayscasting.domain.s3.service.S3Service;
import com.todayscasting.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

// 캐스팅 카드 이미지를 실제로 비동기로 생성/업로드하는 전담 서비스. (이슈 #93)
// CastingCardServiceImpl에서 직접 @Async 메서드를 호출하면(자기 자신 호출) 비동기가 적용되지
// 않으므로, 별도 빈으로 분리했다. 카드 생성 트랜잭션이 커밋된 이후에 호출되며, 완료되면
// 별도 트랜잭션(save())으로 generatedImageKey를 갱신한다.
@Slf4j
@Component
@RequiredArgsConstructor
public class CastingImageAsyncService {

    private static final String GENERATED_IMAGE_DIRECTORY = "casting-images/generated";

    private final ImageGenerationService imageGenerationService;
    private final S3Service s3Service;
    private final CastingCardRepository castingCardRepository;

    @Async
    public void generateAndAttachImage(Long castingCardId, String genre, User.Gender gender, String highlight) {
        try {
            String prompt = imageGenerationService.buildPrompt(genre, gender, highlight);
            byte[] imageBytes = imageGenerationService.generateImage(prompt, gender);

            String key = s3Service.uploadBytes(imageBytes, GENERATED_IMAGE_DIRECTORY, "image/png");

            castingCardRepository.findById(castingCardId).ifPresentOrElse(
                    card -> {
                        card.updateGeneratedImageKey(key);
                        castingCardRepository.save(card);
                        log.info("캐스팅 카드 {} 이미지 비동기 생성 완료", castingCardId);
                    },
                    () -> log.warn("캐스팅 카드 {} 를 찾을 수 없어 이미지 key를 저장하지 못했습니다.", castingCardId)
            );

        } catch (Exception e) {
            // 비동기라 호출부로 예외가 전파되지 않으므로, 실패해도 카드 자체는 이미 응답이 나간 뒤라 영향 없음.
            // generatedImageKey는 계속 null로 남고, 조회 시 CastingImageResolver 폴백으로 처리됨.
            log.error("캐스팅 카드 {} 이미지 비동기 생성 실패. genre={}", castingCardId, genre, e);
        }
    }

}
