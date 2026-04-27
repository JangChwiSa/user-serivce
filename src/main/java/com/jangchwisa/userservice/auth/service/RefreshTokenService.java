package com.jangchwisa.userservice.auth.service;

import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

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
        return REFRESH_TOKEN_KEY_PREFIX + userId;
    }
}
