package com.leetmate.platform.repository;

import com.leetmate.platform.entity.Challenge;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Repository;

/**
 * In-memory persistence for {@link Challenge} records.
 */
@Repository
public class ChallengeRepository {

    private final ConcurrentMap<UUID, Challenge> storage = new ConcurrentHashMap<>();

    /**
     * Saves the challenge instance.
     *
     * @param challenge challenge to save
     * @return same challenge
     */
    public Challenge save(Challenge challenge) {
        storage.put(challenge.getId(), challenge);
        return challenge;
    }

    /**
     * Loads a challenge by identifier.
     *
     * @param id identifier
     * @return optional challenge
     */
    public Optional<Challenge> findById(UUID id) {
        return Optional.ofNullable(storage.get(id));
    }

    /**
     * Finds all challenges belonging to a given group.
     *
     * @param groupId group identifier
     * @return ordered list
     */
    public List<Challenge> findByGroupId(UUID groupId) {
        return storage.values().stream()
                .filter(challenge -> challenge.getGroupId().equals(groupId))
                .sorted(Comparator.comparing(Challenge::getCreatedAt).reversed())
                .toList();
    }

    /**
     * @return total count
     */
    public long count() {
        return storage.size();
    }

    /**
     * For test resets.
     */
    public void deleteAll() {
        storage.clear();
    }
}
