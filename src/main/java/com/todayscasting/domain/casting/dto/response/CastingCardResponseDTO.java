package com.todayscasting.domain.casting.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class CastingCardResponseDTO {

    private CastingCardStatus status;
    private Boolean hasCastingCard;
    private Boolean hasGeneratedImage;

    private Long id;
    private Long dailyRecordId;
    private Long castingImageId;

    // 기존 매칭 방식(CastingImageResolver)으로 나온 이미지일 때만 채워지는 필드.
    // 이건 이미 바로 접속 가능한 고정 URL이라 프론트에서 그대로 쓰면 된다. (이슈 #93)
    private String imageUrl;

    // 실시간 생성된 이미지가 있을 때만 채워지는 필드. presigned URL이 아니라 S3 객체 key 그대로다.
    // 프론트는 이 값이 있으면 GET /castings/image-url?key={imageKey} 로 화면에 그리기 직전에
    // presigned URL을 새로 받아서 써야 한다 (URL을 미리 받아서 오래 캐싱해두면 유효기간이 지나서
    // 깨질 수 있으므로, 실제로 보여줄 때마다 새로 요청하는 것을 권장). (이슈 #93)
    private String imageKey;

    private String genre;
    private String roleName;
    private String highlight;
    private String oneLineComment;
    private String scenePhrase;
    private String commentPhrase;
    private List<String> additionalMood;
    private String characterPhrase;
    private Boolean isFavorite;
    private LocalDateTime generatedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
