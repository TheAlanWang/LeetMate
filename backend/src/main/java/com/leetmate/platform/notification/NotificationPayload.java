package com.leetmate.platform.notification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NotificationPayload(
        @NotNull Long targetUserId,
        @NotBlank String message
) {
}
