package com.leetmate.platform.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for completing a password reset.
 */
public class ResetPasswordRequest {

    @NotBlank(message = "token must not be blank")
    private String token;

    @NotBlank(message = "newPassword must not be blank")
    @Size(min = 6, max = 100, message = "newPassword must be between 6 and 100 characters")
    private String newPassword;

    public ResetPasswordRequest() {
    }

    public ResetPasswordRequest(String token, String newPassword) {
        this.token = token;
        this.newPassword = newPassword;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
