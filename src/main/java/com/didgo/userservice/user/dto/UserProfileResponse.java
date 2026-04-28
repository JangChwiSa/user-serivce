package com.didgo.userservice.user.dto;

import java.time.LocalDate;
import java.util.List;

public record UserProfileResponse(
        Long userId,
        String loginId,
        String name,
        LocalDate birthDate,
        String gender,
        String email,
        List<String> disabilities,
        String desiredJob,
        String accountStatus
) {
}
