package com.didgo.userservice.auth.service;

import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    // 문서에 정의된 Redis 키 규칙을 그대로 사용한다.
    private static final String REFRESH_TOKEN_KEY_PREFIX = "refresh_token:";

    private final StringRedisTemplate stringRedisTemplate;

    public void save(Long userId, String refreshToken, Duration ttl) {
        stringRedisTemplate.opsForValue().set(buildKey(userId), refreshToken, ttl);
    }

    public Optional<String> get(Long userId) {
        return Optional.ofNullable(stringRedisTemplate.opsForValue().get(buildKey(userId)));
    }

    public void delete(Long userId) {
        stringRedisTemplate.delete(buildKey(userId));
    }

    private String buildKey(Long userId) {
        // 사용자별로 Refresh Token을 한 개만 유지하도록 키를 고정한다.
        return REFRESH_TOKEN_KEY_PREFIX + userId;
    }
}
