package com.jangchwisa.userservice.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jangchwisa.userservice.auth.dto.LoginRequest;
import com.jangchwisa.userservice.auth.dto.ReissueTokenRequest;
import com.jangchwisa.userservice.auth.dto.SignupRequest;
import com.jangchwisa.userservice.common.exception.BusinessException;
import com.jangchwisa.userservice.common.exception.ErrorCode;
import com.jangchwisa.userservice.security.JwtProperties;
import com.jangchwisa.userservice.security.JwtTokenProvider;
import com.jangchwisa.userservice.user.domain.AccountStatus;
import com.jangchwisa.userservice.user.domain.Gender;
import com.jangchwisa.userservice.user.domain.User;
import com.jangchwisa.userservice.user.domain.UserDisability;
import com.jangchwisa.userservice.user.repository.UserDisabilityRepository;
import com.jangchwisa.userservice.user.repository.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserDisabilityRepository userDisabilityRepository;

    @Mock
    private RefreshTokenService refreshTokenService;

    private PasswordEncoder passwordEncoder;
    private JwtTokenProvider jwtTokenProvider;
    private Clock clock;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.parse("2026-04-27T14:00:00Z"), ZoneOffset.UTC);
        passwordEncoder = new BCryptPasswordEncoder();
        jwtTokenProvider = new JwtTokenProvider(
                new JwtProperties(
                        "test-secret-key-for-jwt-signing-at-least-32-bytes",
                        "test-user-service",
                        3600,
                        1209600,
                        2592000
                ),
                clock
        );
        authService = new AuthService(
                userRepository,
                userDisabilityRepository,
                passwordEncoder,
                jwtTokenProvider,
                refreshTokenService,
                clock
        );
    }

    @Test
    void signupCreatesUserAndDisabilities() {
        SignupRequest request = new SignupRequest(
                "user01",
                "password1234",
                "홍길동",
                LocalDate.of(2000, 1, 1),
                Gender.MALE,
                "user@example.com",
                List.of("발달장애", "청각장애"),
                "사무직"
        );
        User savedUser = User.create(
                request.loginId(),
                passwordEncoder.encode(request.password()),
                request.name(),
                request.birthDate(),
                request.gender(),
                request.email(),
                request.desiredJob(),
                LocalDateTime.now(clock)
        );
        setUserId(savedUser, 1L);

        when(userRepository.existsByLoginId(request.loginId())).thenReturn(false);
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        var response = authService.signup(request);

        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.message()).isEqualTo("회원가입이 완료되었습니다.");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPasswordHash()).isNotEqualTo(request.password());
        assertThat(passwordEncoder.matches(request.password(), userCaptor.getValue().getPasswordHash())).isTrue();

        ArgumentCaptor<List<UserDisability>> disabilityCaptor = ArgumentCaptor.forClass(List.class);
        verify(userDisabilityRepository).saveAll(disabilityCaptor.capture());
        assertThat(disabilityCaptor.getValue()).extracting(UserDisability::getDisabilityType)
                .containsExactly("발달장애", "청각장애");
    }

    @Test
    void loginStoresRefreshTokenAndReturnsTokens() {
        User user = createPersistedUser(1L, "user01", AccountStatus.ACTIVE);
        when(userRepository.findByLoginId("user01")).thenReturn(Optional.of(user));
        when(userDisabilityRepository.findAllByUserUserIdOrderByDisabilityIdAsc(1L))
                .thenReturn(List.of(
                        UserDisability.create(user, "발달장애", LocalDateTime.now(clock)),
                        UserDisability.create(user, "청각장애", LocalDateTime.now(clock))
                ));

        var response = authService.login(new LoginRequest("user01", "password1234", true));

        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.refreshToken()).isNotBlank();
        assertThat(response.user().userId()).isEqualTo(1L);
        assertThat(response.user().disabilities()).containsExactly("발달장애", "청각장애");
        verify(refreshTokenService).save(eq(1L), eq(response.refreshToken()), any());
    }

    @Test
    void loginRejectsLockedAccount() {
        User user = createPersistedUser(1L, "user01", AccountStatus.LOCKED);
        when(userRepository.findByLoginId("user01")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(new LoginRequest("user01", "password1234", false)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.LOCKED_ACCOUNT);

        verify(refreshTokenService, never()).save(eq(1L), any(), any());
    }

    @Test
    void reissueFailsWhenRefreshTokenDoesNotMatchRedisValue() {
        String refreshToken = jwtTokenProvider.createRefreshToken(1L, false);
        when(refreshTokenService.get(1L)).thenReturn(Optional.of("different-token"));

        assertThatThrownBy(() -> authService.reissue(new ReissueTokenRequest(refreshToken)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.REFRESH_TOKEN_MISMATCH);
    }

    @Test
    void logoutDeletesRefreshToken() {
        var response = authService.logout(1L);

        assertThat(response.message()).isEqualTo("로그아웃이 완료되었습니다.");
        verify(refreshTokenService).delete(1L);
    }

    private User createPersistedUser(Long userId, String loginId, AccountStatus status) {
        User user = User.create(
                loginId,
                passwordEncoder.encode("password1234"),
                "홍길동",
                LocalDate.of(2000, 1, 1),
                Gender.MALE,
                "user@example.com",
                "사무직",
                LocalDateTime.now(clock)
        );
        setUserId(user, userId);
        user.changeStatus(status, LocalDateTime.now(clock));
        return user;
    }

    private void setUserId(User user, Long userId) {
        try {
            var field = User.class.getDeclaredField("userId");
            field.setAccessible(true);
            field.set(user, userId);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
