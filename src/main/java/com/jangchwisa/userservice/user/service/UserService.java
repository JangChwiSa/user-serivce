package com.jangchwisa.userservice.user.service;

import com.jangchwisa.userservice.common.exception.BusinessException;
import com.jangchwisa.userservice.common.exception.ErrorCode;
import com.jangchwisa.userservice.user.domain.User;
import com.jangchwisa.userservice.user.domain.UserDisability;
import com.jangchwisa.userservice.user.dto.InternalUserResponse;
import com.jangchwisa.userservice.user.dto.UpdateUserProfileRequest;
import com.jangchwisa.userservice.user.dto.UpdateUserProfileResponse;
import com.jangchwisa.userservice.user.dto.UserProfileResponse;
import com.jangchwisa.userservice.user.repository.UserDisabilityRepository;
import com.jangchwisa.userservice.user.repository.UserRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserDisabilityRepository userDisabilityRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile(Long userId) {
        User user = getUser(userId);
        List<String> disabilities = getDisabilities(userId);
        return new UserProfileResponse(
                user.getUserId(),
                user.getLoginId(),
                user.getName(),
                user.getBirthDate(),
                user.getGender() != null ? user.getGender().name() : null,
                user.getEmail(),
                disabilities,
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

        // 문서 기준으로 장애 정보는 부분 수정이 아니라 전체 교체 방식으로 관리한다.
        userDisabilityRepository.deleteAllByUserUserId(userId);
        saveDisabilities(user, request.disabilities(), now);

        return new UpdateUserProfileResponse("사용자 정보가 수정되었습니다.");
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
                getDisabilities(userId),
                user.getDesiredJob()
        );
    }

    private void validateEmailDuplication(User user, String email) {
        if (!user.getEmail().equals(email) && userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.DUPLICATED_EMAIL);
        }
    }

    private void saveDisabilities(User user, List<String> disabilities, LocalDateTime now) {
        List<UserDisability> disabilityEntities = disabilities.stream()
                // 수정 요청에도 중복 값이 들어올 수 있어 저장 전에 정규화한다.
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

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
