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

    // 회원 생성과 초기 장애 정보 저장을 한 트랜잭션으로 처리한다.
    @PostMapping("/signup")
    public SignupResponse signup(@Valid @RequestBody SignupRequest request) {
        return authService.signup(request);
    }

    // 로그인 성공 시 Access/Refresh Token과 사용자 요약 정보를 함께 반환한다.
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    // 현재 Access Token의 사용자 식별값으로 Redis의 Refresh Token을 제거한다.
    @PostMapping("/logout")
    public LogoutResponse logout(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return authService.logout(authenticatedUser.userId());
    }

    // 재발급은 전달된 Refresh Token과 Redis 저장값이 모두 일치해야 한다.
    @PostMapping("/reissue")
    public ReissueTokenResponse reissue(@Valid @RequestBody ReissueTokenRequest request) {
        return authService.reissue(request);
    }
}
