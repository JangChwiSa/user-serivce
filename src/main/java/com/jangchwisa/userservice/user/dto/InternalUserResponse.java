package com.jangchwisa.userservice.user.dto;

import java.util.List;

public record InternalUserResponse(
        Long userId,
        String loginId,
        String name,
        String email,
        String accountStatus,
        List<String> disabilities,
        String desiredJob
) {
}
