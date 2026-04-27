package com.jangchwisa.userservice.auth.controller;

import com.jangchwisa.userservice.auth.dto.LoginRequest;
import com.jangchwisa.userservice.auth.dto.LoginResponse;
import com.jangchwisa.userservice.auth.dto.LogoutResponse;
import com.jangchwisa.userservice.auth.dto.ReissueTokenRequest;
import com.jangchwisa.userservice.auth.dto.ReissueTokenResponse;
import com.jangchwisa.userservice.auth.dto.SignupRequest;
import com.jangchwisa.userservice.auth.dto.SignupResponse;
import com.jangchwisa.userservice.auth.service.AuthService;
import com.jangchwisa.userservice.security.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public SignupResponse signup(@Valid @RequestBody SignupRequest request) {
        return authService.signup(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/logout")
    public LogoutResponse logout(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return authService.logout(authenticatedUser.userId());
    }

    @PostMapping("/reissue")
    public ReissueTokenResponse reissue(@Valid @RequestBody ReissueTokenRequest request) {
        return authService.reissue(request);
    }
}
