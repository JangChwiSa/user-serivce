package com.didgo.userservice.auth.dto;

public record ReissueTokenResponse(
        String accessToken,
        String refreshToken
) {
}
