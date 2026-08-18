package com.todayscasting.domain.casting.dto.response;

import lombok.Builder;
import lombok.Getter;

// S3 key를 받아서 presigned URL로 변환해주는 API의 응답. (이슈 #93)
@Getter
@Builder
public class ImageUrlResponseDTO {

    private String imageUrl;

}