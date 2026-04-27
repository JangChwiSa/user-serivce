package com.jangchwisa.userservice.common.api;

public record ApiErrorResponse(
        String code,
        String message
) {
}
