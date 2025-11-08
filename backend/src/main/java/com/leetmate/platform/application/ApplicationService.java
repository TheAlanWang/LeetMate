package com.leetmate.platform.application;

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
 * Coordinates the mentee application lifecycle, ensuring per-group uniqueness and
 * promoting approved mentees into memberships as mandated by the PRD.
 */
@Service
public class ApplicationService {

    private final GroupApplicationRepository repository;
    private final GroupService groupService;
    private final UserService userService;
    private final MembershipService membershipService;

    public ApplicationService(GroupApplicationRepository repository,
                              GroupService groupService,
                              UserService userService,
                              MembershipService membershipService) {
        this.repository = repository;
        this.groupService = groupService;
        this.userService = userService;
        this.membershipService = membershipService;
    }

    @Transactional
    public ApplicationResponse submitApplication(Long groupId, SubmitApplicationRequest request) {
        StudyGroup group = groupService.getGroupEntity(groupId);
        User mentee = userService.getUserOrThrow(request.menteeId());
        if (mentee.getRole() != UserRole.MENTEE) {
            throw new IllegalArgumentException("Only mentees can apply to groups");
        }
        repository.findByGroupAndMentee(group, mentee)
                .ifPresent(existing -> {
                    throw new IllegalStateException("Mentee already applied to this group");
                });

        GroupApplication application = new GroupApplication();
        application.setGroup(group);
        application.setMentee(mentee);
        application.setMessage(request.message());
        application.setExperienceLevel(request.experienceLevel());
        application.setAvailability(request.availability());

        return ApplicationResponse.fromEntity(repository.save(application));
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> listApplicationsForGroup(Long groupId, Long mentorId) {
        StudyGroup group = groupService.getGroupEntity(groupId);
        if (!group.getMentor().getId().equals(mentorId)) {
            throw new IllegalArgumentException("Only the owning mentor can review applications");
        }
        return repository.findByGroup(group).stream()
                .map(ApplicationResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> listApplicationsForUser(Long userId) {
        User mentee = userService.getUserOrThrow(userId);
        return repository.findByMentee(mentee).stream()
                .map(ApplicationResponse::fromEntity)
                .toList();
    }

    @Transactional
    public ApplicationResponse reviewApplication(Long applicationId, UpdateApplicationStatusRequest request) {
        GroupApplication application = repository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application %d not found".formatted(applicationId)));

        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new IllegalStateException("Application already reviewed");
        }

        ApplicationStatus newStatus = request.status();
        if (newStatus == ApplicationStatus.APPROVED) {
            membershipService.ensureMembership(application.getGroup(), application.getMentee());
        }
        application.setStatus(newStatus);
        return ApplicationResponse.fromEntity(repository.save(application));
    }
}
