package com.jangchwisa.userservice.auth.service;

import com.jangchwisa.userservice.auth.dto.LoginRequest;
import com.jangchwisa.userservice.auth.dto.LoginResponse;
import com.jangchwisa.userservice.auth.dto.LogoutResponse;
import com.jangchwisa.userservice.auth.dto.ReissueTokenRequest;
import com.jangchwisa.userservice.auth.dto.ReissueTokenResponse;
import com.jangchwisa.userservice.auth.dto.SignupRequest;
import com.jangchwisa.userservice.auth.dto.SignupResponse;
import com.jangchwisa.userservice.common.exception.BusinessException;
import com.jangchwisa.userservice.common.exception.ErrorCode;
import com.jangchwisa.userservice.security.JwtTokenProvider;
import com.jangchwisa.userservice.user.domain.AccountStatus;
import com.jangchwisa.userservice.user.domain.User;
import com.jangchwisa.userservice.user.domain.UserDisability;
import com.jangchwisa.userservice.user.repository.UserDisabilityRepository;
import com.jangchwisa.userservice.user.repository.UserRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserDisabilityRepository userDisabilityRepository;
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
        saveDisabilities(savedUser, request.disabilities(), now);

        return new SignupResponse(savedUser.getUserId(), "회원가입이 완료되었습니다.");
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

        List<String> disabilities = getDisabilities(user.getUserId());
        LoginResponse.UserLoginView userView = new LoginResponse.UserLoginView(
                user.getUserId(),
                user.getLoginId(),
                user.getName(),
                user.getEmail(),
                disabilities,
                user.getDesiredJob()
        );
        return new LoginResponse(accessToken, refreshToken, userView);
    }

    public LogoutResponse logout(Long userId) {
        refreshTokenService.delete(userId);
        return new LogoutResponse("로그아웃이 완료되었습니다.");
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

    private void saveDisabilities(User user, List<String> disabilities, LocalDateTime now) {
        List<UserDisability> disabilityEntities = disabilities.stream()
                .distinct()
                .map(disability -> UserDisability.create(user, disability, now))
                .toList();
        userDisabilityRepository.saveAll(disabilityEntities);
    }

    private List<String> getDisabilities(Long userId) {
        return userDisabilityRepository.findAllByUserUserIdOrderByDisabilityIdAsc(userId).stream()
                .map(UserDisability::getDisabilityType)
                .toList();
    }
}
