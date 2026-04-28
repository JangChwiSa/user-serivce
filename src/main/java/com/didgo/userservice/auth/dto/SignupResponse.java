package com.didgo.userservice.auth.dto;

public record SignupResponse(
        Long userId,
        String message
) {
}
