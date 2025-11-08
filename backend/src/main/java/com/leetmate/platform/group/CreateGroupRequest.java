package com.leetmate.platform.group;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateGroupRequest(
        @NotBlank String title,
        @NotBlank String topic,
        @NotBlank String level,
        @NotNull Long mentorId,
        @NotBlank String description,
        @Min(1) int capacity
) {
}
