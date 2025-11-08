package com.leetmate.platform.invite;

import com.leetmate.platform.group.StudyGroup;
import com.leetmate.platform.user.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MentorInviteRepository extends JpaRepository<MentorInvite, Long> {

    List<MentorInvite> findByMentee(User mentee);

    List<MentorInvite> findByGroup(StudyGroup group);
}
