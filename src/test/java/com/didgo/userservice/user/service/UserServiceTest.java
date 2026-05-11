package com.didgo.userservice.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.didgo.userservice.common.exception.BusinessException;
import com.didgo.userservice.common.exception.ErrorCode;
import com.didgo.userservice.user.domain.AccountStatus;
import com.didgo.userservice.user.domain.Gender;
import com.didgo.userservice.user.domain.User;
import com.didgo.userservice.user.dto.UpdateUserProfileRequest;
import com.didgo.userservice.user.repository.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private Clock clock;

    private UserService userService;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.parse("2026-04-27T14:00:00Z"), ZoneOffset.UTC);
        userService = new UserService(userRepository, clock);
    }

    @Test
    void getMyProfileReturnsUser() {
        User user = createPersistedUser(1L, "user01", "user@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        var response = userService.getMyProfile(1L);

        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.loginId()).isEqualTo("user01");
        assertThat(response.desiredJob()).isEqualTo("Office Clerk");
        assertThat(response.accountStatus()).isEqualTo(AccountStatus.ACTIVE.name());
    }

    @Test
    void updateMyProfileUpdatesUser() {
        User user = createPersistedUser(1L, "user01", "user@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);

        var request = new UpdateUserProfileRequest(
                "Kim Yuna",
                Gender.FEMALE,
                "new@example.com",
                "Retail Clerk"
        );

        var response = userService.updateMyProfile(1L, request);

        assertThat(response.message()).isEqualTo("사용자 정보가 수정되었습니다.");
        assertThat(user.getName()).isEqualTo("Kim Yuna");
        assertThat(user.getEmail()).isEqualTo("new@example.com");
        assertThat(user.getDesiredJob()).isEqualTo("Retail Clerk");
    }

    @Test
    void updateMyProfileRejectsDuplicatedEmail() {
        User user = createPersistedUser(1L, "user01", "user@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("duplicated@example.com")).thenReturn(true);

        var request = new UpdateUserProfileRequest(
                "Hong Gil-dong",
                Gender.MALE,
                "duplicated@example.com",
                "Office Clerk"
        );

        assertThatThrownBy(() -> userService.updateMyProfile(1L, request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DUPLICATED_EMAIL);
    }

    @Test
    void getInternalUserReturnsMinimalUserPayload() {
        User user = createPersistedUser(1L, "user01", "user@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        var response = userService.getInternalUser(1L);

        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.loginId()).isEqualTo("user01");
        assertThat(response.desiredJob()).isEqualTo("Office Clerk");
    }

    private User createPersistedUser(Long userId, String loginId, String email) {
        User user = User.create(
                loginId,
                "encoded-password",
                "Hong Gil-dong",
                LocalDate.of(2000, 1, 1),
                Gender.MALE,
                email,
                "Office Clerk",
                LocalDateTime.now(clock)
        );
        setUserId(user, userId);
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
