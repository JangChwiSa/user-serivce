package com.didgo.userservice.user.service;

import com.didgo.userservice.common.exception.BusinessException;
import com.didgo.userservice.common.exception.ErrorCode;
import com.didgo.userservice.user.domain.User;
import com.didgo.userservice.user.dto.InternalUserResponse;
import com.didgo.userservice.user.dto.UpdateUserProfileRequest;
import com.didgo.userservice.user.dto.UpdateUserProfileResponse;
import com.didgo.userservice.user.dto.UserProfileResponse;
import com.didgo.userservice.user.repository.UserRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile(Long userId) {
        User user = getUser(userId);
        return new UserProfileResponse(
                user.getUserId(),
                user.getLoginId(),
                user.getName(),
                user.getBirthDate(),
                user.getGender() != null ? user.getGender().name() : null,
                user.getEmail(),
                user.getDesiredJob(),
                user.getStatus().name()
        );
    }

    @Transactional
    public UpdateUserProfileResponse updateMyProfile(Long userId, UpdateUserProfileRequest request) {
        User user = getUser(userId);
        validateEmailDuplication(user, request.email());

        LocalDateTime now = LocalDateTime.now(clock);
        user.updateProfile(request.name(), request.gender(), request.email(), request.desiredJob(), now);

        return new UpdateUserProfileResponse("?ъ슜???뺣낫媛 ?섏젙?섏뿀?듬땲??");
    }

    @Transactional(readOnly = true)
    public InternalUserResponse getInternalUser(Long userId) {
        User user = getUser(userId);
        return new InternalUserResponse(
                user.getUserId(),
                user.getLoginId(),
                user.getName(),
                user.getEmail(),
                user.getStatus().name(),
                user.getDesiredJob()
        );
    }

    private void validateEmailDuplication(User user, String email) {
        if (!user.getEmail().equals(email) && userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.DUPLICATED_EMAIL);
        }
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
