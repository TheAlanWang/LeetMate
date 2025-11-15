package com.leetmate.platform.repository;

import com.leetmate.platform.entity.PasswordResetToken;
import com.leetmate.platform.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for password reset tokens.
 */
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    Optional<PasswordResetToken> findByTokenAndUsedAtIsNull(UUID token);

    void deleteAllByUser(User user);
}
