package com.leetmate.platform.dto.group;

import java.time.Instant;
import java.util.UUID;

/**
 * Representation of a mentee who follows a group.
 *
 * @param id       mentee identifier
 * @param name     mentee display name
 * @param email    mentee email
 * @param joinedAt join timestamp
 */
public record GroupMemberResponse(UUID id, String name, String email, Instant joinedAt) {
}
