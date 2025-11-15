package com.leetmate.platform.controller;

import com.leetmate.platform.dto.auth.AuthResponse;
import com.leetmate.platform.dto.auth.ForgotPasswordRequest;
import com.leetmate.platform.dto.auth.LoginRequest;
import com.leetmate.platform.dto.auth.RegisterRequest;
import com.leetmate.platform.dto.auth.ResetPasswordRequest;
import com.leetmate.platform.dto.common.MessageResponse;
import com.leetmate.platform.service.AuthService;
import com.leetmate.platform.service.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication endpoints for mentors and mentees.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    public AuthController(AuthService authService, PasswordResetService passwordResetService) {
        this.authService = authService;
        this.passwordResetService = passwordResetService;
    }

    /**
     * Registers a new user.
     *
     * @param request payload
     * @return auth response
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    /**
     * Logs in an existing user.
     *
     * @param request login payload
     * @return auth response
     */
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    /**
     * Initiates the forgot password flow.
     *
     * @param request payload
     * @return message response
     */
    @PostMapping("/password/forgot")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public MessageResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.requestReset(request.getEmail());
        return new MessageResponse("If that email is registered, reset instructions are on the way.");
    }

    /**
     * Completes a password reset using a token.
     *
     * @param request payload
     * @return message response
     */
    @PostMapping("/password/reset")
    public MessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        return new MessageResponse("Password updated successfully.");
    }
}
