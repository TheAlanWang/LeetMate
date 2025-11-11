package com.leetmate.platform.repository;

import com.leetmate.platform.entity.Submission;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Repository;

/**
 * In-memory persistence for submissions.
 */
@Repository
public class SubmissionRepository {

    private final ConcurrentMap<UUID, Submission> storage = new ConcurrentHashMap<>();

    /**
     * Saves a submission.
     *
     * @param submission submission instance
     * @return persisted submission
     */
    public Submission save(Submission submission) {
        storage.put(submission.getId(), submission);
        return submission;
    }

    /**
     * Retrieves a submission by identifier.
     *
     * @param id identifier
     * @return optional submission
     */
    public Optional<Submission> findById(UUID id) {
        return Optional.ofNullable(storage.get(id));
    }

    /**
     * Lists submissions for a challenge ordered by recency.
     *
     * @param challengeId challenge identifier
     * @return submissions
     */
    public List<Submission> findByChallengeId(UUID challengeId) {
        return storage.values().stream()
                .filter(submission -> submission.getChallengeId().equals(challengeId))
                .sorted(Comparator.comparing(Submission::getCreatedAt).reversed())
                .toList();
    }

    /**
     * Clears repository state. Used in tests.
     */
    public void deleteAll() {
        storage.clear();
    }
}
