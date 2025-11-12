package com.leetmate.platform.dto.auth;

/**
 * Authentication response containing JWT and user summary.
 *
 * @param token JWT token
 * @param user  user summary
 */
public record AuthResponse(String token, UserSummary user) {
}
