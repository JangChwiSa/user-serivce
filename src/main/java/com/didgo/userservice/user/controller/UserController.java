package com.didgo.userservice.user.controller;

import com.didgo.userservice.security.AuthenticatedUser;
import com.didgo.userservice.user.dto.UpdateUserProfileRequest;
import com.didgo.userservice.user.dto.UpdateUserProfileResponse;
import com.didgo.userservice.user.dto.UserProfileResponse;
import com.didgo.userservice.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // SecurityContext에 들어 있는 현재 사용자 기준으로 내 정보를 조회한다.
    @GetMapping("/me")
    public UserProfileResponse getMyProfile(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return userService.getMyProfile(authenticatedUser.userId());
    }

    // 요청 본문은 DTO로 받고, 실제 수정 대상 userId는 토큰에서만 가져온다.
    @PatchMapping("/me")
    public UpdateUserProfileResponse updateMyProfile(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody UpdateUserProfileRequest request
    ) {
        return userService.updateMyProfile(authenticatedUser.userId(), request);
    }
}
