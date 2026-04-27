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

    @GetMapping("/{userId}")
    public InternalUserResponse getInternalUser(@PathVariable Long userId) {
        return userService.getInternalUser(userId);
    }
}
