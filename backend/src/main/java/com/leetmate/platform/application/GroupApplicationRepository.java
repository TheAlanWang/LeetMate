package com.leetmate.platform.application;

import com.leetmate.platform.group.StudyGroup;
import com.leetmate.platform.user.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupApplicationRepository extends JpaRepository<GroupApplication, Long> {

    List<GroupApplication> findByGroup(StudyGroup group);

    List<GroupApplication> findByMentee(User mentee);

    Optional<GroupApplication> findByGroupAndMentee(StudyGroup group, User mentee);
}
