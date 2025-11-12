package com.leetmate.platform.service;

import com.leetmate.platform.dto.common.PageResponse;
import com.leetmate.platform.dto.group.CreateGroupRequest;
import com.leetmate.platform.dto.group.GroupResponse;
import com.leetmate.platform.entity.GroupMember;
import com.leetmate.platform.entity.StudyGroup;
import com.leetmate.platform.entity.User;
import com.leetmate.platform.entity.UserRole;
import com.leetmate.platform.exception.ResourceNotFoundException;
import com.leetmate.platform.repository.GroupMemberRepository;
import com.leetmate.platform.repository.StudyGroupRepository;
import com.leetmate.platform.repository.UserRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * Application layer for managing study groups.
 */
@Service
public class GroupService {

    private static final int MAX_PAGE_SIZE = 100;

    private final StudyGroupRepository repository;
    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;

    /**
     * Creates a new service with the in-memory repository.
     *
     * @param repository group repository
     */
    public GroupService(StudyGroupRepository repository,
                        UserRepository userRepository,
                        GroupMemberRepository groupMemberRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.groupMemberRepository = groupMemberRepository;
    }

    /**
     * Creates a group from the given request.
     *
     * @param request creation payload
     * @return created group
     */
    public GroupResponse createGroup(CreateGroupRequest request, UUID mentorId) {
        var mentor = userRepository.findById(mentorId)
                .orElseThrow(() -> new ResourceNotFoundException("User %s not found".formatted(mentorId)));
        StudyGroup group = new StudyGroup(UUID.randomUUID(), mentor,
                request.getName(),
                request.getDescription(),
                request.getTags(),
                Instant.now());
        repository.save(group);
        return toResponse(group);
    }

    /**
     * Lists groups with pagination.
     *
     * @param page requested page
     * @param size requested size
     * @return paginated response
     */
    public PageResponse<GroupResponse> listGroups(int page, int size) {
        validatePagination(page, size);
        PageRequest pageable = PageRequest.of(page, size);
        Page<GroupResponse> groupPage = repository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::toResponse);
        return new PageResponse<>(
                groupPage.getContent(),
                groupPage.getNumber(),
                groupPage.getSize(),
                groupPage.getTotalElements(),
                groupPage.getTotalPages());
    }

    /**
     * Returns a group by identifier.
     *
     * @param groupId identifier
     * @return response
     */
    public GroupResponse getGroup(UUID groupId) {
        return toResponse(find(groupId));
    }

    /**
     * Increments member count when a mentee joins.
     *
     * @param groupId identifier
     * @return updated response
     */
    public GroupResponse joinGroup(UUID groupId, UUID menteeId) {
        StudyGroup group = find(groupId);
        User mentee = findMentee(menteeId);
        if (groupMemberRepository.existsByGroupIdAndMemberId(groupId, menteeId)) {
            throw new IllegalStateException("You already joined this group");
        }
        GroupMember membership = new GroupMember(UUID.randomUUID(), group, mentee, Instant.now());
        groupMemberRepository.save(membership);
        group.incrementMembers();
        repository.save(group);
        return toResponse(group);
    }

    /**
     * Decrements member count when a mentee leaves.
     *
     * @param groupId identifier
     * @return updated response
     */
    public GroupResponse leaveGroup(UUID groupId, UUID menteeId) {
        StudyGroup group = find(groupId);
        var membership = groupMemberRepository.findByGroupIdAndMemberId(groupId, menteeId)
                .orElseThrow(() -> new ResourceNotFoundException("You are not a member of this group"));
        groupMemberRepository.delete(membership);
        group.decrementMembers();
        repository.save(group);
        return toResponse(group);
    }

    private StudyGroup find(UUID groupId) {
        return repository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group %s not found".formatted(groupId)));
    }

    private GroupResponse toResponse(StudyGroup group) {
        return new GroupResponse(group.getId(), group.getName(), group.getDescription(),
                group.getTags(), group.getMemberCount(), group.getCreatedAt(),
                group.getMentor() != null ? group.getMentor().getId() : null,
                group.getMentor() != null ? group.getMentor().getName() : null);
    }

    private void validatePagination(int page, int size) {
        Assert.isTrue(page >= 0, "page must be greater or equal to 0");
        Assert.isTrue(size > 0 && size <= MAX_PAGE_SIZE,
                "size must be between 1 and " + MAX_PAGE_SIZE);
    }

    private User findMentee(UUID menteeId) {
        User mentee = userRepository.findById(menteeId)
                .orElseThrow(() -> new ResourceNotFoundException("User %s not found".formatted(menteeId)));
        if (mentee.getRole() != UserRole.MENTEE) {
            throw new IllegalStateException("Only mentees can join groups");
        }
        return mentee;
    }
}
