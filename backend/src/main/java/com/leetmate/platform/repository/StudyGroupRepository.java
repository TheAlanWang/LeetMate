package com.leetmate.platform.repository;

import com.leetmate.platform.entity.StudyGroup;
import java.util.UUID;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA repository for {@link StudyGroup}.
 */
public interface StudyGroupRepository extends JpaRepository<StudyGroup, UUID> {

    Page<StudyGroup> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<StudyGroup> findAllByMentorIdOrderByCreatedAtDesc(UUID mentorId);
}
