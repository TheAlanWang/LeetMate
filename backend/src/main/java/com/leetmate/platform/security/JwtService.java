package com.leetmate.platform.security;

import com.leetmate.platform.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Utility for issuing and validating JWT tokens.
 */
@Component
public class JwtService {

    private final SecretKey secretKey;
    private final Duration expiration;

    public JwtService(@Value("${security.jwt.secret:this-should-be-overridden}") String secret,
                      @Value("${security.jwt.expiration:PT24H}") Duration expiration) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    /**
     * Generates a JWT for the provided user.
     *
     * @param user user instance
     * @return signed token
     */
    public String generateToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("userId", user.getId().toString())
                .claim("role", user.getRole().name())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(expiration)))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Validates and parses a JWT.
     *
     * @param token bearer token
     * @return optional claims
     */
    public Optional<Claims> parse(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return Optional.of(claims);
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    /**
     * Extracts the user identifier stored in the token.
     *
     * @param claims token claims
     * @return user id
     */
    public Optional<UUID> extractUserId(Claims claims) {
        String userId = claims.get("userId", String.class);
        if (userId == null) {
            return Optional.empty();
        }
        return Optional.of(UUID.fromString(userId));
    }
}
