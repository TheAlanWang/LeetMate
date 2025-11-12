package com.leetmate.platform.service;

import com.leetmate.platform.dto.auth.AuthResponse;
import com.leetmate.platform.dto.auth.LoginRequest;
import com.leetmate.platform.dto.auth.RegisterRequest;
import com.leetmate.platform.dto.auth.UserSummary;
import com.leetmate.platform.entity.User;
import com.leetmate.platform.entity.UserRole;
import com.leetmate.platform.repository.UserRepository;
import com.leetmate.platform.security.JwtService;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Handles registration and login flows.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * Registers a new mentor or mentee.
     *
     * @param request payload
     * @return auth response
     */
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }
        UserRole role = parseRole(request.getRole());
        User user = new User(
                UUID.randomUUID(),
                request.getName(),
                request.getEmail().toLowerCase(Locale.ROOT),
                passwordEncoder.encode(request.getPassword()),
                role,
                Instant.now());
        userRepository.save(user);
        return new AuthResponse(jwtService.generateToken(user), toSummary(user));
    }

    /**
     * Logs in an existing user.
     *
     * @param request login request
     * @return auth response
     */
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        return new AuthResponse(jwtService.generateToken(user), toSummary(user));
    }

    private UserSummary toSummary(User user) {
        return new UserSummary(user.getId(), user.getName(), user.getEmail(), user.getRole().name());
    }

    private UserRole parseRole(String role) {
        if (!StringUtils.hasText(role)) {
            throw new IllegalArgumentException("role must not be blank");
        }
        try {
            return UserRole.valueOf(role.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("role must be MENTOR or MENTEE");
        }
    }
}
