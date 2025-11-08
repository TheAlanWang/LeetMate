package com.leetmate.platform.application;

import jakarta.validation.constraints.NotNull;

public record UpdateApplicationStatusRequest(
        @NotNull ApplicationStatus status
) {
}
