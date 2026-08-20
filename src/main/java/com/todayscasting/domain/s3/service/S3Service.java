package com.todayscasting.domain.s3.service;

import java.io.IOException;
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Service
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucket;
    private final String region;

    public S3Service(
            S3Client s3Client,
            S3Presigner s3Presigner,
            @Value("${aws.s3.bucket}") String bucket,
            @Value("${aws.s3.region}") String region
    ) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.bucket = bucket;
        this.region = region;
    }

    /**
     * 지정한 디렉터리에 파일을 업로드하고 S3 객체 키를 반환합니다.
     */
    public String upload(MultipartFile file, String directory) {
        String key = createObjectKey(file, directory);

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            return key;
        } catch (IOException e) {
            throw new IllegalStateException("S3 업로드 파일을 읽을 수 없습니다.", e);
        }
    }

    /**
     * byte[] 데이터를 지정한 디렉터리에 업로드하고 S3 객체 키를 반환합니다.
     * MultipartFile이 없는 경우(예: 외부 API가 생성한 이미지 바이트를 그대로 저장할 때) 사용합니다. (이슈 #93)
     */
    public String uploadBytes(byte[] data, String directory, String contentType) {
        String key = createObjectKeyForBytes(directory, contentType);

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .contentLength((long) data.length)
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(data));
        return key;
    }

    /**
     * S3 객체 키를 기준으로 파일을 삭제합니다.
     */
    public void delete(String key) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        s3Client.deleteObject(request);
    }

    /**
     * private 버킷 객체를 임시로 조회할 수 있는 presigned URL을 생성합니다.
     */
    public String createPresignedGetUrl(String key, Duration duration) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(duration)
                .getObjectRequest(getObjectRequest)
                .build();

        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    /**
     * S3 객체를 서버에서 직접 읽어 byte[]로 반환합니다.
     */
    public byte[] getObjectBytes(String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        ResponseBytes<GetObjectResponse> responseBytes = s3Client.getObjectAsBytes(getObjectRequest);
        return responseBytes.asByteArray();
    }

    /**
     * public 버킷 또는 CloudFront 전환 전 기본 S3 공개 URL을 생성합니다.
     */
    public String createPublicGetUrl(String key) {
        String normalizedKey = Objects.requireNonNull(key, "key").replaceAll("^/+", "");
        return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + normalizedKey;
    }

    /**
     * 업로드 디렉터리와 UUID를 조합해 중복 가능성이 낮은 객체 키를 만듭니다.
     */
    private String createObjectKey(MultipartFile file, String directory) {
        String filename = Objects.requireNonNullElse(file.getOriginalFilename(), "file");
        String extension = extractExtension(filename);
        String normalizedDirectory = normalizeDirectory(directory);
        return normalizedDirectory + "/" + UUID.randomUUID() + extension;
    }

    /**
     * byte[] 업로드용 객체 키를 만듭니다. contentType이 image/png일 때만 .png 확장자를 붙입니다.
     */
    private String createObjectKeyForBytes(String directory, String contentType) {
        String extension = "image/png".equals(contentType) ? ".png" : "";
        String normalizedDirectory = normalizeDirectory(directory);
        return normalizedDirectory + "/" + UUID.randomUUID() + extension;
    }

    /**
     * 원본 파일명에서 확장자를 추출합니다.
     */
    private String extractExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0) {
            return "";
        }
        return filename.substring(dotIndex);
    }

    /**
     * S3 키에 사용할 디렉터리 문자열을 정리합니다.
     */
    private String normalizeDirectory(String directory) {
        String normalized = Objects.requireNonNullElse(directory, "uploads").trim();
        if (normalized.isBlank()) {
            return "uploads";
        }
        return normalized.replaceAll("^/+", "").replaceAll("/+$", "");
    }
}
