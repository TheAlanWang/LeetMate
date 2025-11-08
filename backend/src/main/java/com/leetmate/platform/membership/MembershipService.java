package com.leetmate.platform.membership;

import com.leetmate.platform.group.StudyGroup;
import com.leetmate.platform.user.User;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MembershipService {

    private final MembershipRepository repository;

    public MembershipService(MembershipRepository repository) {
        this.repository = repository;
    }

    public List<Membership> getMembers(StudyGroup group) {
        return repository.findByGroup(group);
    }

    public Membership ensureMembership(StudyGroup group, User user) {
        return repository.findByGroupAndUser(group, user)
                .orElseGet(() -> createMembership(group, user));
    }

    private Membership createMembership(StudyGroup group, User user) {
        Membership membership = new Membership();
        membership.setGroup(group);
        membership.setUser(user);
        membership.setJoinedAt(OffsetDateTime.now());
        return repository.save(membership);
    }
}
