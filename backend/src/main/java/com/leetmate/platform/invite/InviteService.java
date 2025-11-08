package com.leetmate.platform.invite;

import com.leetmate.platform.group.GroupService;
import com.leetmate.platform.group.StudyGroup;
import com.leetmate.platform.membership.MembershipService;
import com.leetmate.platform.user.User;
import com.leetmate.platform.user.UserRole;
import com.leetmate.platform.user.UserService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles mentor-driven invitations, ensuring invitations align with mentor ownership
 * and that accepted invites convert into group memberships.
 */
@Service
public class InviteService {

    private final MentorInviteRepository repository;
    private final GroupService groupService;
    private final UserService userService;
    private final MembershipService membershipService;

    public InviteService(MentorInviteRepository repository,
                         GroupService groupService,
                         UserService userService,
                         MembershipService membershipService) {
        this.repository = repository;
        this.groupService = groupService;
        this.userService = userService;
        this.membershipService = membershipService;
    }

    @Transactional
    public InviteResponse sendInvite(Long groupId, SendInviteRequest request) {
        StudyGroup group = groupService.getGroupEntity(groupId);
        User mentor = userService.getUserOrThrow(request.mentorId());
        User mentee = userService.getUserOrThrow(request.menteeId());

        if (!group.getMentor().getId().equals(mentor.getId())) {
            throw new IllegalArgumentException("Mentor does not own this group");
        }
        if (mentor.getRole() != UserRole.MENTOR || mentee.getRole() != UserRole.MENTEE) {
            throw new IllegalArgumentException("Invalid roles for invite flow");
        }

        MentorInvite invite = new MentorInvite();
        invite.setGroup(group);
        invite.setMentor(mentor);
        invite.setMentee(mentee);
        return InviteResponse.fromEntity(repository.save(invite));
    }

    @Transactional(readOnly = true)
    public List<InviteResponse> listInvitesForUser(Long userId) {
        User mentee = userService.getUserOrThrow(userId);
        return repository.findByMentee(mentee).stream()
                .map(InviteResponse::fromEntity)
                .toList();
    }

    @Transactional
    public InviteResponse respondToInvite(Long inviteId, InviteDecisionRequest request) {
        MentorInvite invite = repository.findById(inviteId)
                .orElseThrow(() -> new IllegalArgumentException("Invite %d not found".formatted(inviteId)));
        if (invite.getStatus() != InviteStatus.PENDING) {
            throw new IllegalStateException("Invite already processed");
        }

        InviteStatus status = request.status();
        invite.setStatus(status);
        MentorInvite saved = repository.save(invite);

        if (status == InviteStatus.ACCEPTED) {
            membershipService.ensureMembership(invite.getGroup(), invite.getMentee());
        }
        return InviteResponse.fromEntity(saved);
    }

    @Transactional
    public void deleteInvite(Long inviteId, Long mentorId) {
        MentorInvite invite = repository.findById(inviteId)
                .orElseThrow(() -> new IllegalArgumentException("Invite %d not found".formatted(inviteId)));
        if (!invite.getMentor().getId().equals(mentorId)) {
            throw new IllegalArgumentException("Only owning mentor can cancel invite");
        }
        invite.setStatus(InviteStatus.CANCELLED);
        repository.save(invite);
    }
}
