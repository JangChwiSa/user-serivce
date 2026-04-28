package com.didgo.userservice.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record ReissueTokenRequest(
        @NotBlank(message = "Refresh Token은 필수입니다.")
        String refreshToken
) {
}
