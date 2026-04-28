package com.didgo.userservice.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    DUPLICATED_LOGIN_ID(HttpStatus.CONFLICT, "DUPLICATED_LOGIN_ID", "이미 사용 중인 아이디입니다."),
    DUPLICATED_EMAIL(HttpStatus.CONFLICT, "DUPLICATED_EMAIL", "이미 사용 중인 이메일입니다."),
    INVALID_LOGIN_ID_OR_PASSWORD(HttpStatus.UNAUTHORIZED, "INVALID_LOGIN_ID_OR_PASSWORD", "아이디 또는 비밀번호가 올바르지 않습니다."),
    LOCKED_ACCOUNT(HttpStatus.FORBIDDEN, "LOCKED_ACCOUNT", "잠긴 계정입니다."),
    WITHDRAWN_ACCOUNT(HttpStatus.FORBIDDEN, "WITHDRAWN_ACCOUNT", "탈퇴한 계정입니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "EXPIRED_TOKEN", "만료된 토큰입니다."),
    REFRESH_TOKEN_MISMATCH(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_MISMATCH", "Refresh Token이 일치하지 않습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "잘못된 요청입니다."),
    INTERNAL_API_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "INTERNAL_API_UNAUTHORIZED", "내부 API 인증에 실패했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public HttpStatus status() {
        return status;
    }

    public String code() {
        return code;
    }

    public String message() {
        return message;
    }
}
