package com.jangchwisa.userservice.user.controller;

import com.jangchwisa.userservice.security.AuthenticatedUser;
import com.jangchwisa.userservice.user.dto.UpdateUserProfileRequest;
import com.jangchwisa.userservice.user.dto.UpdateUserProfileResponse;
import com.jangchwisa.userservice.user.dto.UserProfileResponse;
import com.jangchwisa.userservice.user.service.UserService;
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

    @GetMapping("/me")
    public UserProfileResponse getMyProfile(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return userService.getMyProfile(authenticatedUser.userId());
    }

    @PatchMapping("/me")
    public UpdateUserProfileResponse updateMyProfile(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody UpdateUserProfileRequest request
    ) {
        return userService.updateMyProfile(authenticatedUser.userId(), request);
    }
}
