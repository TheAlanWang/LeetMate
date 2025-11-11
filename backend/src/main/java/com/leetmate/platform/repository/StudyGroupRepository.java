package com.leetmate.platform.repository;

import com.leetmate.platform.entity.StudyGroup;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Repository;

/**
 * In-memory repository for {@link StudyGroup} aggregates.
 */
@Repository
public class StudyGroupRepository {

    private final ConcurrentMap<UUID, StudyGroup> storage = new ConcurrentHashMap<>();

    /**
     * Persists or updates the supplied group.
     *
     * @param group group instance
     * @return persisted group
     */
    public StudyGroup save(StudyGroup group) {
        storage.put(group.getId(), group);
        return group;
    }

    /**
     * Retrieves a group by identifier.
     *
     * @param id identifier
     * @return optional group
     */
    public Optional<StudyGroup> findById(UUID id) {
        return Optional.ofNullable(storage.get(id));
    }

    /**
     * @return all groups sorted by creation time descending
     */
    public List<StudyGroup> findAll() {
        return storage.values().stream()
                .sorted(Comparator.comparing(StudyGroup::getCreatedAt).reversed())
                .toList();
    }

    /**
     * @return number of groups
     */
    public long count() {
        return storage.size();
    }

    /**
     * Clears repository contents. Intended for tests.
     */
    public void deleteAll() {
        storage.clear();
    }
}
