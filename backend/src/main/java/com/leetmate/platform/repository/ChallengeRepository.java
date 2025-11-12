package com.leetmate.platform.repository;

import com.leetmate.platform.entity.Challenge;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA persistence for {@link Challenge} records.
 */
public interface ChallengeRepository extends JpaRepository<Challenge, UUID> {

    List<Challenge> findByGroup_IdOrderByCreatedAtDesc(UUID groupId);
}
