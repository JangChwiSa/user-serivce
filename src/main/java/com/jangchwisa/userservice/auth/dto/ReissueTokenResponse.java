package com.jangchwisa.userservice.auth.dto;

public record ReissueTokenResponse(
        String accessToken,
        String refreshToken
) {
}
