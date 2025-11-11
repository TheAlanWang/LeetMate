package com.leetmate.platform.repository;

import com.leetmate.platform.entity.StudyGroup;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StudyGroupRepositoryTest {

    @Test
    void saveAndFindGroup() {
        StudyGroupRepository repository = new StudyGroupRepository();
        StudyGroup group = new StudyGroup(UUID.randomUUID(), "Graph", "desc", List.of("tag"), Instant.now());
        repository.save(group);

        assertThat(repository.findById(group.getId())).isPresent();
        assertThat(repository.findAll()).hasSize(1);
    }

    @Test
    void countReflectsStoredGroups() {
        StudyGroupRepository repository = new StudyGroupRepository();
        repository.save(new StudyGroup(UUID.randomUUID(), "Graph", "desc", List.of("tag"), Instant.now()));
        repository.save(new StudyGroup(UUID.randomUUID(), "DP", "desc", List.of("tag"), Instant.now()));

        assertThat(repository.count()).isEqualTo(2);
        repository.deleteAll();
        assertThat(repository.count()).isZero();
    }
}
