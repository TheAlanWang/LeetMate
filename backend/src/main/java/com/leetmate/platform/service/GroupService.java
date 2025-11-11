package com.leetmate.platform.service;

import com.leetmate.platform.dto.common.PageResponse;
import com.leetmate.platform.dto.group.CreateGroupRequest;
import com.leetmate.platform.dto.group.GroupResponse;
import com.leetmate.platform.entity.StudyGroup;
import com.leetmate.platform.exception.ResourceNotFoundException;
import com.leetmate.platform.repository.StudyGroupRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * Application layer for managing study groups.
 */
@Service
public class GroupService {

    private static final int MAX_PAGE_SIZE = 100;

    private final StudyGroupRepository repository;

    /**
     * Creates a new service with the in-memory repository.
     *
     * @param repository group repository
     */
    public GroupService(StudyGroupRepository repository) {
        this.repository = repository;
    }

    /**
     * Creates a group from the given request.
     *
     * @param request creation payload
     * @return created group
     */
    public GroupResponse createGroup(CreateGroupRequest request) {
        StudyGroup group = new StudyGroup(UUID.randomUUID(),
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
        List<StudyGroup> groups = repository.findAll();
        int fromIndex = Math.min(page * size, groups.size());
        int toIndex = Math.min(fromIndex + size, groups.size());
        List<GroupResponse> content = groups.subList(fromIndex, toIndex).stream()
                .map(this::toResponse)
                .toList();
        int totalPages = (int) Math.ceil((double) groups.size() / size);
        return new PageResponse<>(content, page, size, groups.size(), totalPages);
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
    public GroupResponse joinGroup(UUID groupId) {
        StudyGroup group = find(groupId);
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
    public GroupResponse leaveGroup(UUID groupId) {
        StudyGroup group = find(groupId);
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
                group.getTags(), group.getMemberCount(), group.getCreatedAt());
    }

    private void validatePagination(int page, int size) {
        Assert.isTrue(page >= 0, "page must be greater or equal to 0");
        Assert.isTrue(size > 0 && size <= MAX_PAGE_SIZE,
                "size must be between 1 and " + MAX_PAGE_SIZE);
    }
}
