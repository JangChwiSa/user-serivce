package com.didgo.userservice.user.repository;

import com.didgo.userservice.user.domain.UserDisability;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface UserDisabilityRepository extends JpaRepository<UserDisability, Long> {

    List<UserDisability> findAllByUserUserIdOrderByDisabilityIdAsc(Long userId);

    @Transactional
    void deleteAllByUserUserId(Long userId);
}
