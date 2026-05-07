package com.didgo.userservice.user.dto;

public record InternalUserResponse(
        Long userId,
        String loginId,
        String name,
        String email,
        String accountStatus,
        String desiredJob
) {
}
