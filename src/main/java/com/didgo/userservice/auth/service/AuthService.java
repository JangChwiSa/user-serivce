package com.didgo.userservice.auth.service;

import com.didgo.userservice.auth.dto.LoginRequest;
import com.didgo.userservice.auth.dto.LoginResponse;
import com.didgo.userservice.auth.dto.LogoutResponse;
import com.didgo.userservice.auth.dto.ReissueTokenRequest;
import com.didgo.userservice.auth.dto.ReissueTokenResponse;
import com.didgo.userservice.auth.dto.SignupRequest;
import com.didgo.userservice.auth.dto.SignupResponse;
import com.didgo.userservice.common.exception.BusinessException;
import com.didgo.userservice.common.exception.ErrorCode;
import com.didgo.userservice.security.JwtTokenProvider;
import com.didgo.userservice.user.domain.AccountStatus;
import com.didgo.userservice.user.domain.User;
import com.didgo.userservice.user.repository.UserRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final Clock clock;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        validateSignupRequest(request);

        LocalDateTime now = LocalDateTime.now(clock);
        User user = User.create(
                request.loginId(),
                passwordEncoder.encode(request.password()),
                request.name(),
                request.birthDate(),
                request.gender(),
                request.email(),
                request.desiredJob(),
                now
        );
        User savedUser = userRepository.save(user);

        return new SignupResponse(savedUser.getUserId(), "?뚯썝媛?낆씠 ?꾨즺?섏뿀?듬땲??");
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByLoginId(request.loginId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_LOGIN_ID_OR_PASSWORD));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_LOGIN_ID_OR_PASSWORD);
        }

        validateAccountStatus(user);

        user.updateLastLoginAt(LocalDateTime.now(clock));

        String accessToken = jwtTokenProvider.createAccessToken(user.getUserId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getUserId(), request.rememberMe());
        long refreshTokenExpiration = jwtTokenProvider.getRefreshTokenExpirationSeconds(request.rememberMe());
        refreshTokenService.save(user.getUserId(), refreshToken, Duration.ofSeconds(refreshTokenExpiration));

        LoginResponse.UserLoginView userView = new LoginResponse.UserLoginView(
                user.getUserId(),
                user.getLoginId(),
                user.getName(),
                user.getEmail(),
                user.getDesiredJob()
        );
        return new LoginResponse(accessToken, refreshToken, userView);
    }

    public LogoutResponse logout(Long userId) {
        refreshTokenService.delete(userId);
        return new LogoutResponse("濡쒓렇?꾩썐???꾨즺?섏뿀?듬땲??");
    }

    @Transactional(readOnly = true)
    public ReissueTokenResponse reissue(ReissueTokenRequest request) {
        jwtTokenProvider.validateRefreshToken(request.refreshToken());
        Long userId = jwtTokenProvider.extractUserId(request.refreshToken());

        String savedToken = refreshTokenService.get(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REFRESH_TOKEN_MISMATCH));

        if (!savedToken.equals(request.refreshToken())) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_MISMATCH);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        validateAccountStatus(user);

        String newAccessToken = jwtTokenProvider.createAccessToken(userId);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(userId, false);
        refreshTokenService.save(
                userId,
                newRefreshToken,
                Duration.ofSeconds(jwtTokenProvider.getRefreshTokenExpirationSeconds(false))
        );

        return new ReissueTokenResponse(newAccessToken, newRefreshToken);
    }

    private void validateSignupRequest(SignupRequest request) {
        if (userRepository.existsByLoginId(request.loginId())) {
            throw new BusinessException(ErrorCode.DUPLICATED_LOGIN_ID);
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.DUPLICATED_EMAIL);
        }
    }

    private void validateAccountStatus(User user) {
        if (user.getStatus() == AccountStatus.LOCKED) {
            throw new BusinessException(ErrorCode.LOCKED_ACCOUNT);
        }
        if (user.getStatus() == AccountStatus.WITHDRAWN) {
            throw new BusinessException(ErrorCode.WITHDRAWN_ACCOUNT);
        }
    }
}
