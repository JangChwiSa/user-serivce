package com.jangchwisa.userservice.security;

import com.jangchwisa.userservice.common.exception.BusinessException;
import com.jangchwisa.userservice.common.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private static final String TOKEN_TYPE_CLAIM = "tokenType";

    private final JwtProperties jwtProperties;
    private final Clock clock;
    private final SecretKey secretKey;

    public JwtTokenProvider(JwtProperties jwtProperties, Clock clock) {
        this.jwtProperties = jwtProperties;
        this.clock = clock;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(Long userId) {
        return createToken(userId, TokenType.ACCESS, Duration.ofSeconds(jwtProperties.accessTokenExpirationSeconds()));
    }

    public String createRefreshToken(Long userId, boolean rememberMe) {
        long expirationSeconds = rememberMe
                ? jwtProperties.rememberMeRefreshTokenExpirationSeconds()
                : jwtProperties.refreshTokenExpirationSeconds();
        return createToken(userId, TokenType.REFRESH, Duration.ofSeconds(expirationSeconds));
    }

    public long getRefreshTokenExpirationSeconds(boolean rememberMe) {
        return rememberMe
                ? jwtProperties.rememberMeRefreshTokenExpirationSeconds()
                : jwtProperties.refreshTokenExpirationSeconds();
    }

    public Long extractUserId(String token) {
        Claims claims = parseClaims(token);
        return Long.parseLong(claims.getSubject());
    }

    public void validateAccessToken(String token) {
        validateToken(token, TokenType.ACCESS);
    }

    public void validateRefreshToken(String token) {
        validateToken(token, TokenType.REFRESH);
    }

    private String createToken(Long userId, TokenType tokenType, Duration duration) {
        Instant now = Instant.now(clock);
        Instant expiration = now.plus(duration);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuer(jwtProperties.issuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .claim(TOKEN_TYPE_CLAIM, tokenType.name())
                .signWith(secretKey)
                .compact();
    }

    private void validateToken(String token, TokenType expectedType) {
        Claims claims = parseClaims(token);
        String tokenType = claims.get(TOKEN_TYPE_CLAIM, String.class);
        if (!expectedType.name().equals(tokenType)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException exception) {
            throw new BusinessException(ErrorCode.EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
    }
}
