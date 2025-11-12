package com.leetmate.platform.repository;

import com.leetmate.platform.entity.Challenge;
import com.leetmate.platform.entity.ChallengeDifficulty;
import com.leetmate.platform.entity.StudyGroup;
import com.leetmate.platform.entity.User;
import com.leetmate.platform.entity.UserRole;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ChallengeRepositoryTest {

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private StudyGroupRepository studyGroupRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByGroupIdFiltersProperly() {
        User mentor = userRepository.save(new User(UUID.randomUUID(), "Mentor", "mentor@demo.com", "hash", UserRole.MENTOR, Instant.now()));
        StudyGroup group = studyGroupRepository.save(new StudyGroup(UUID.randomUUID(), mentor, "Graph", "desc", List.of("tag"), Instant.now()));
        challengeRepository.save(new Challenge(UUID.randomUUID(), group, "Two Sum", "desc", "java",
                ChallengeDifficulty.EASY, "code", Instant.now()));
        StudyGroup otherGroup = studyGroupRepository.save(new StudyGroup(UUID.randomUUID(), mentor, "Other", "desc", List.of("tag"), Instant.now()));
        challengeRepository.save(new Challenge(UUID.randomUUID(), otherGroup, "Other", "desc", "java",
                ChallengeDifficulty.MEDIUM, "code", Instant.now()));

        List<Challenge> challenges = challengeRepository.findByGroup_IdOrderByCreatedAtDesc(group.getId());
        assertThat(challenges).hasSize(1);
        assertThat(challenges.get(0).getTitle()).isEqualTo("Two Sum");
    }
}
