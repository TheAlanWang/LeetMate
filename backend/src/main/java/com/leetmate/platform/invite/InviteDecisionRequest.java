package com.leetmate.platform.invite;

import jakarta.validation.constraints.NotNull;

public record InviteDecisionRequest(
        @NotNull InviteStatus status
) {
}
