package com.leetmate.platform.application;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SubmitApplicationRequest(
        @NotNull Long menteeId,
        @NotBlank String message,
        @NotBlank String experienceLevel,
        @NotBlank String availability
) {
}
