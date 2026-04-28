package com.didgo.userservice.common.api;

public record ApiErrorResponse(
        String code,
        String message
) {
}
