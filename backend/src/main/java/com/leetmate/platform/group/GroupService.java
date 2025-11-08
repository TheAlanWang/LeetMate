package com.leetmate.platform.group;

import com.leetmate.platform.membership.MemberResponse;
import com.leetmate.platform.membership.MembershipService;
import com.leetmate.platform.user.User;
import com.leetmate.platform.user.UserRole;
import com.leetmate.platform.user.UserService;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GroupService {

    private final StudyGroupRepository repository;
    private final UserService userService;
    private final MembershipService membershipService;

    public GroupService(StudyGroupRepository repository,
                        UserService userService,
                        MembershipService membershipService) {
        this.repository = repository;
        this.userService = userService;
        this.membershipService = membershipService;
    }

    @Transactional
    public GroupResponse createGroup(CreateGroupRequest request) {
        User mentor = userService.getUserOrThrow(request.mentorId());
        if (mentor.getRole() != UserRole.MENTOR) {
            throw new IllegalArgumentException("Only mentors can create groups");
        }

        StudyGroup group = new StudyGroup();
        group.setTitle(request.title());
        group.setTopic(request.topic());
        group.setLevel(request.level());
        group.setMentor(mentor);
        group.setDescription(request.description());
        group.setCapacity(request.capacity());
        StudyGroup saved = repository.save(group);

        // Ensure mentor is a member for downstream queries
        membershipService.ensureMembership(saved, mentor);

        return GroupResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<GroupResponse> listGroups() {
        return repository.findAll()
                .stream()
                .map(GroupResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GroupResponse getGroup(Long id) {
        return GroupResponse.fromEntity(getGroupEntity(id));
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> getMembers(Long groupId) {
        StudyGroup group = getGroupEntity(groupId);
        return membershipService.getMembers(group)
                .stream()
                .map(MemberResponse::fromMembership)
                .toList();
    }

    public StudyGroup getGroupEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Group %d not found".formatted(id)));
    }
}
