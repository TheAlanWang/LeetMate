package com.leetmate.platform.notification;

import java.time.OffsetDateTime;

public record NotificationEvent(
        Long targetUserId,
        String message,
        OffsetDateTime createdAt
) {
}
