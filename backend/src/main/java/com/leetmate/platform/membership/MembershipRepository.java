package com.leetmate.platform.membership;

import com.leetmate.platform.group.StudyGroup;
import com.leetmate.platform.user.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipRepository extends JpaRepository<Membership, Long> {

    List<Membership> findByGroup(StudyGroup group);

    Optional<Membership> findByGroupAndUser(StudyGroup group, User user);
}
