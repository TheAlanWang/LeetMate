package com.leetmate.platform.invite;

import jakarta.validation.constraints.NotNull;

public record SendInviteRequest(
        @NotNull Long mentorId,
        @NotNull Long menteeId
) {
}
