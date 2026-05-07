package com.didgo.userservice.auth.dto;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        UserLoginView user
) {
    public record UserLoginView(
            Long userId,
            String loginId,
            String name,
            String email,
            String desiredJob
    ) {
    }
}
