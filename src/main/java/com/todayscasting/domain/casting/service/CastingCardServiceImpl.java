package com.todayscasting.domain.casting.service;

import com.todayscasting.common.code.status.ErrorStatus;
import com.todayscasting.common.exception.GeneralException;
import com.todayscasting.domain.casting.converter.CastingCardConverter;
import com.todayscasting.domain.casting.dto.request.CastingCardRequestDTO;
import com.todayscasting.domain.casting.dto.response.CastingCardResponseDTO;
import com.todayscasting.domain.casting.entity.CastingCard;
import com.todayscasting.domain.casting.repository.CastingCardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CastingCardServiceImpl implements CastingCardService {

    private final CastingCardRepository castingCardRepository;

    @Override
    @Transactional
    public CastingCardResponseDTO createCastingCard(CastingCardRequestDTO request) {

        // TODO: AI 분석 결과(ai_analysis_logs)를 조회해서 실제 title/genre/roleName/score 등을 채워야 함
        //       AI 서버 완성 전까지는 임시 값으로 생성
        CastingCard castingCard = CastingCard.builder()
                .dailyRecordId(request.getDailyRecordId())
                .title("임시 제목")
                .genre("임시 장르")
                .roleName("임시 배역")
                .subtitle("임시 부제목")
                .highlight("임시 하이라이트")
                .oneLineComment("임시 코멘트")
                .score(80)
                .analysisSummary("임시 분석 요약")
                .build();

        CastingCard savedCastingCard = castingCardRepository.save(castingCard);

        return CastingCardConverter.toResponseDTO(savedCastingCard);
    }

    @Override
    @Transactional(readOnly = true)
    public CastingCardResponseDTO getCastingCard(Long dailyRecordId) {
        CastingCard castingCard = findByDailyRecordIdOrThrow(dailyRecordId);
        return CastingCardConverter.toResponseDTO(castingCard);
    }

    @Override
    @Transactional
    public CastingCardResponseDTO toggleFavorite(Long dailyRecordId) {
        CastingCard castingCard = findByDailyRecordIdOrThrow(dailyRecordId);
        castingCard.toggleFavorite();
        return CastingCardConverter.toResponseDTO(castingCard);
    }

    private CastingCard findByDailyRecordIdOrThrow(Long dailyRecordId) {
        return castingCardRepository.findByDailyRecordId(dailyRecordId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.RESOURCE_NOT_FOUND));
    }

}