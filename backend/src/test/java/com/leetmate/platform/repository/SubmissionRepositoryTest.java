package com.leetmate.platform.repository;

import com.leetmate.platform.entity.Submission;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SubmissionRepositoryTest {

    @Test
    void findByChallengeIdReturnsOrderedResults() {
        SubmissionRepository repository = new SubmissionRepository();
        UUID challengeId = UUID.randomUUID();
        Submission first = new Submission(UUID.randomUUID(), challengeId, "java", "code", 1, Instant.now());
        Submission second = new Submission(UUID.randomUUID(), challengeId, "java", "code", 1,
                Instant.now().plusSeconds(5));
        repository.save(first);
        repository.save(second);

        List<Submission> submissions = repository.findByChallengeId(challengeId);
        assertThat(submissions).hasSize(2);
        assertThat(submissions.get(0).getCreatedAt()).isAfter(submissions.get(1).getCreatedAt());
    }
}
