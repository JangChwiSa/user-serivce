package com.jangchwisa.userservice.auth.dto;

import java.util.List;

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
            List<String> disabilities,
            String desiredJob
    ) {
    }
}
