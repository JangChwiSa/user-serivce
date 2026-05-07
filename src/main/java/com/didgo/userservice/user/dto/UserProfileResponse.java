package com.didgo.userservice.user.dto;

import java.time.LocalDate;

public record UserProfileResponse(
        Long userId,
        String loginId,
        String name,
        LocalDate birthDate,
        String gender,
        String email,
        String desiredJob,
        String accountStatus
) {
}
