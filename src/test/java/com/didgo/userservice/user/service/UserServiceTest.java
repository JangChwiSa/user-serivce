package com.didgo.userservice.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.didgo.userservice.common.exception.BusinessException;
import com.didgo.userservice.common.exception.ErrorCode;
import com.didgo.userservice.user.domain.AccountStatus;
import com.didgo.userservice.user.domain.Gender;
import com.didgo.userservice.user.domain.User;
import com.didgo.userservice.user.domain.UserDisability;
import com.didgo.userservice.user.dto.UpdateUserProfileRequest;
import com.didgo.userservice.user.repository.UserDisabilityRepository;
import com.didgo.userservice.user.repository.UserRepository;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserDisabilityRepository userDisabilityRepository;

    private Clock clock;

    private UserService userService;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.parse("2026-04-27T14:00:00Z"), ZoneOffset.UTC);
        userService = new UserService(userRepository, userDisabilityRepository, clock);
    }

    @Test
    void getMyProfileReturnsUserAndDisabilities() {
        User user = createPersistedUser(1L, "user01", "user@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userDisabilityRepository.findAllByUserUserIdOrderByDisabilityIdAsc(1L))
                .thenReturn(List.of(
                        UserDisability.create(user, "발달장애", LocalDateTime.now(clock)),
                        UserDisability.create(user, "청각장애", LocalDateTime.now(clock))
                ));

        var response = userService.getMyProfile(1L);

        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.loginId()).isEqualTo("user01");
        assertThat(response.disabilities()).containsExactly("발달장애", "청각장애");
        assertThat(response.accountStatus()).isEqualTo(AccountStatus.ACTIVE.name());
    }

    @Test
    void updateMyProfileUpdatesUserAndReplacesDisabilities() {
        User user = createPersistedUser(1L, "user01", "user@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);

        var request = new UpdateUserProfileRequest(
                "임꺽정",
                Gender.FEMALE,
                "new@example.com",
                List.of("지체장애", "지체장애", "발달장애"),
                "단순 노무"
        );

        var response = userService.updateMyProfile(1L, request);

        assertThat(response.message()).isEqualTo("사용자 정보가 수정되었습니다.");
        assertThat(user.getName()).isEqualTo("임꺽정");
        assertThat(user.getEmail()).isEqualTo("new@example.com");
        assertThat(user.getDesiredJob()).isEqualTo("단순 노무");

        verify(userDisabilityRepository).deleteAllByUserUserId(1L);
        ArgumentCaptor<List<UserDisability>> disabilityCaptor = ArgumentCaptor.forClass(List.class);
        verify(userDisabilityRepository).saveAll(disabilityCaptor.capture());
        assertThat(disabilityCaptor.getValue()).extracting(UserDisability::getDisabilityType)
                .containsExactly("지체장애", "발달장애");
    }

    @Test
    void updateMyProfileRejectsDuplicatedEmail() {
        User user = createPersistedUser(1L, "user01", "user@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("duplicated@example.com")).thenReturn(true);

        var request = new UpdateUserProfileRequest(
                "홍길동",
                Gender.MALE,
                "duplicated@example.com",
                List.of("발달장애"),
                "사무직"
        );

        assertThatThrownBy(() -> userService.updateMyProfile(1L, request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DUPLICATED_EMAIL);

        verify(userDisabilityRepository, never()).deleteAllByUserUserId(eq(1L));
        verify(userDisabilityRepository, never()).saveAll(any());
    }

    @Test
    void getInternalUserReturnsMinimalUserPayload() {
        User user = createPersistedUser(1L, "user01", "user@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userDisabilityRepository.findAllByUserUserIdOrderByDisabilityIdAsc(1L))
                .thenReturn(List.of(UserDisability.create(user, "발달장애", LocalDateTime.now(clock))));

        var response = userService.getInternalUser(1L);

        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.loginId()).isEqualTo("user01");
        assertThat(response.disabilities()).containsExactly("발달장애");
    }

    private User createPersistedUser(Long userId, String loginId, String email) {
        User user = User.create(
                loginId,
                "encoded-password",
                "홍길동",
                LocalDate.of(2000, 1, 1),
                Gender.MALE,
                email,
                "사무직",
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
