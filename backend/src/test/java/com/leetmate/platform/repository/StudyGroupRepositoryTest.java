package com.leetmate.platform.repository;

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
class StudyGroupRepositoryTest {

    @Autowired
    private StudyGroupRepository studyGroupRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void saveAndFindGroup() {
        User mentor = saveMentor("mentor1@demo.com");
        StudyGroup group = studyGroupRepository.save(new StudyGroup(UUID.randomUUID(), mentor,
                "Graph", "desc", List.of("tag"), Instant.now()));

        assertThat(studyGroupRepository.findById(group.getId())).isPresent();
        assertThat(studyGroupRepository.findAll()).hasSize(1);
    }

    @Test
    void countReflectsStoredGroups() {
        User mentor = saveMentor("mentor2@demo.com");
        studyGroupRepository.save(new StudyGroup(UUID.randomUUID(), mentor, "Graph", "desc", List.of("tag"), Instant.now()));
        studyGroupRepository.save(new StudyGroup(UUID.randomUUID(), mentor, "DP", "desc", List.of("tag"), Instant.now()));

        assertThat(studyGroupRepository.count()).isEqualTo(2);
        studyGroupRepository.deleteAll();
        assertThat(studyGroupRepository.count()).isZero();
    }

    private User saveMentor(String email) {
        User user = new User(UUID.randomUUID(), "Mentor", email, "hash", UserRole.MENTOR, Instant.now());
        return userRepository.save(user);
    }
}
