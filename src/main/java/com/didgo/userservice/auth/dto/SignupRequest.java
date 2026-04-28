package com.didgo.userservice.auth.dto;

import com.didgo.userservice.user.domain.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public record SignupRequest(
        @NotBlank(message = "아이디는 필수입니다.")
        @Size(max = 50, message = "아이디는 50자 이하여야 합니다.")
        String loginId,

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 8, max = 100, message = "비밀번호는 8자 이상 100자 이하여야 합니다.")
        String password,

        @NotBlank(message = "이름은 필수입니다.")
        @Size(max = 100, message = "이름은 100자 이하여야 합니다.")
        String name,

        @NotNull(message = "생년월일은 필수입니다.")
        LocalDate birthDate,

        @NotNull(message = "성별은 필수입니다.")
        Gender gender,

        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email,

        @NotEmpty(message = "장애 정보는 최소 1개 이상이어야 합니다.")
        List<@NotBlank(message = "장애 유형은 비어 있을 수 없습니다.") String> disabilities,

        @NotBlank(message = "희망 직무는 필수입니다.")
        @Size(max = 100, message = "희망 직무는 100자 이하여야 합니다.")
        String desiredJob
) {
}
