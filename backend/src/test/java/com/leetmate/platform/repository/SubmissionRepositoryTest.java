package com.leetmate.platform.repository;

import com.leetmate.platform.entity.Challenge;
import com.leetmate.platform.entity.ChallengeDifficulty;
import com.leetmate.platform.entity.StudyGroup;
import com.leetmate.platform.entity.Submission;
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
class SubmissionRepositoryTest {

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private StudyGroupRepository studyGroupRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByChallengeIdReturnsOrderedResults() {
        User mentor = userRepository.save(new User(UUID.randomUUID(), "Mentor", "mentor@demo.com", "hash", UserRole.MENTOR, Instant.now()));
        StudyGroup group = studyGroupRepository.save(new StudyGroup(UUID.randomUUID(), mentor, "Graph", "desc", List.of("tag"), Instant.now()));
        Challenge challenge = challengeRepository.save(new Challenge(UUID.randomUUID(), group, "Two Sum", "desc", "java",
                ChallengeDifficulty.EASY, "code", Instant.now()));
        User mentee = userRepository.save(new User(UUID.randomUUID(), "Mentee", "mentee@demo.com", "hash", UserRole.MENTEE, Instant.now()));

        Submission first = submissionRepository.save(new Submission(UUID.randomUUID(), challenge, mentee, "java", "code", 1, Instant.now()));
        Submission second = submissionRepository.save(new Submission(UUID.randomUUID(), challenge, mentee, "java", "code", 1,
                Instant.now().plusSeconds(5)));

        List<Submission> submissions = submissionRepository.findByChallenge_IdOrderByCreatedAtDesc(challenge.getId(), org.springframework.data.domain.PageRequest.of(0, 10)).getContent();
        assertThat(submissions).hasSize(2);
        assertThat(submissions.get(0).getCreatedAt()).isAfter(submissions.get(1).getCreatedAt());
    }
}
