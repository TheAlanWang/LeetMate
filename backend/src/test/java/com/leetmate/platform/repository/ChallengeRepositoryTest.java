package com.leetmate.platform.repository;

import com.leetmate.platform.entity.Challenge;
import com.leetmate.platform.entity.ChallengeDifficulty;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChallengeRepositoryTest {

    @Test
    void findByGroupIdFiltersProperly() {
        ChallengeRepository repository = new ChallengeRepository();
        UUID groupId = UUID.randomUUID();
        repository.save(new Challenge(UUID.randomUUID(), groupId, "Two Sum", "desc", "java",
                ChallengeDifficulty.EASY, "code", Instant.now()));
        repository.save(new Challenge(UUID.randomUUID(), UUID.randomUUID(), "Other", "desc", "java",
                ChallengeDifficulty.MEDIUM, "code", Instant.now()));

        List<Challenge> challenges = repository.findByGroupId(groupId);
        assertThat(challenges).hasSize(1);
    }
}
