package com.leetmate.platform.service.notification;

import com.leetmate.platform.entity.User;
import java.util.UUID;

/**
 * Contract for delivering password reset links to users.
 */
public interface PasswordResetNotifier {

    void sendResetLink(User user, UUID token);
}
