package com.leetmate.platform.dto.auth;

import java.util.UUID;

/**
 * Lightweight user representation returned to clients.
 *
 * @param id    identifier
 * @param name  display name
 * @param email email address
 * @param role  role string
 */
public record UserSummary(UUID id, String name, String email, String role) {
}
