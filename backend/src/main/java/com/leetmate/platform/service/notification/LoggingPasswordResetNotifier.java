package com.leetmate.platform.service.notification;

import com.leetmate.platform.entity.User;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Dev-friendly notifier that logs password reset links.
 */
@Component
public class LoggingPasswordResetNotifier implements PasswordResetNotifier {

    private static final Logger log = LoggerFactory.getLogger(LoggingPasswordResetNotifier.class);

    private final String linkTemplate;

    public LoggingPasswordResetNotifier(
            @Value("${app.password-reset.link-template:http://localhost:3000/login?resetToken=%s}") String linkTemplate) {
        this.linkTemplate = linkTemplate;
    }

    @Override
    public void sendResetLink(User user, UUID token) {
        String link = String.format(linkTemplate, token);
        log.info("Password reset requested for {}. Send reset link: {}", user.getEmail(), link);
    }
}
