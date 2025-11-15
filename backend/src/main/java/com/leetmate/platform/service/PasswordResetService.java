package com.leetmate.platform.service;

import com.leetmate.platform.entity.PasswordResetToken;
import com.leetmate.platform.entity.User;
import com.leetmate.platform.repository.PasswordResetTokenRepository;
import com.leetmate.platform.repository.UserRepository;
import com.leetmate.platform.service.notification.PasswordResetNotifier;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles password reset request and confirmation flows.
 */
@Service
public class PasswordResetService {

    private static final Duration TOKEN_TTL = Duration.ofHours(1);

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetNotifier notifier;

    public PasswordResetService(UserRepository userRepository,
                                PasswordResetTokenRepository tokenRepository,
                                PasswordEncoder passwordEncoder,
                                PasswordResetNotifier notifier) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.notifier = notifier;
    }

    /**
     * Issues a password reset token and notifies the user.
     *
     * @param email user email
     */
    @Transactional
    public void requestReset(String email) {
        Optional<User> maybeUser = userRepository.findByEmailIgnoreCase(email);
        if (maybeUser.isEmpty()) {
            return;
        }
        User user = maybeUser.get();
        tokenRepository.deleteAllByUser(user);
        Instant now = Instant.now();
        PasswordResetToken token = new PasswordResetToken(UUID.randomUUID(), user, now, now.plus(TOKEN_TTL));
        tokenRepository.save(token);
        notifier.sendResetLink(user, token.getToken());
    }

    /**
     * Resets a user's password if the provided token is valid.
     *
     * @param rawToken token string
     * @param newPassword desired password
     */
    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        UUID tokenValue;
        try {
            tokenValue = UUID.fromString(rawToken);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Reset token is invalid or expired");
        }
        PasswordResetToken token = tokenRepository.findByTokenAndUsedAtIsNull(tokenValue)
                .orElseThrow(() -> new IllegalArgumentException("Reset token is invalid or expired"));
        Instant now = Instant.now();
        if (token.isExpired(now)) {
            throw new IllegalArgumentException("Reset token is invalid or expired");
        }
        User user = token.getUser();
        user.updatePassword(passwordEncoder.encode(newPassword));
        token.markUsed(now);
        userRepository.save(user);
        tokenRepository.save(token);
    }
}
