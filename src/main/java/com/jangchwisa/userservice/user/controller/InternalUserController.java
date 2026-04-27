package com.jangchwisa.userservice.user.controller;

import com.jangchwisa.userservice.user.dto.InternalUserResponse;
import com.jangchwisa.userservice.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserService userService;

    // gRPC 전환 전까지 로컬 검증이나 임시 호환에 사용하는 REST 어댑터다.
    @GetMapping("/{userId}")
    public InternalUserResponse getInternalUser(@PathVariable Long userId) {
        return userService.getInternalUser(userId);
    }
}
