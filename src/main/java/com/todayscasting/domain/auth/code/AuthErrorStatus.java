
package com.todayscasting.domain.auth.code;

import com.todayscasting.common.code.BaseErrorCode;
import com.todayscasting.common.code.ErrorReasonDTO;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorStatus implements BaseErrorCode {

    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "AUTH_409_1", "이미 사용 중인 이메일입니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "AUTH_401_1", "비밀번호가 올바르지 않습니다."),
    AUTH_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH_404_1", "인증 정보를 찾을 수 없습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_401_2", "유효하지 않은 토큰입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH_404_2", "존재하지 않는 사용자입니다."),
    EMAIL_NOT_VERIFIED(HttpStatus.FORBIDDEN, "AUTH_403_1", "이메일 인증이 필요합니다."),
    INVALID_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "AUTH_400_1", "유효하지 않거나 만료된 인증 코드입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDTO getReason() {
        return new ErrorReasonDTO(status, code, message);
    }
}