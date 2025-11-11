package com.leetmate.platform.service;

import com.leetmate.platform.dto.group.CreateGroupRequest;
import com.leetmate.platform.dto.group.GroupResponse;
import com.leetmate.platform.exception.ResourceNotFoundException;
import com.leetmate.platform.repository.StudyGroupRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GroupServiceTest {

    private GroupService groupService;
    private StudyGroupRepository repository;

    @BeforeEach
    void setUp() {
        repository = new StudyGroupRepository();
        groupService = new GroupService(repository);
    }

    @Test
    void createGroupPersistsAndReturnsResponse() {
        CreateGroupRequest request = new CreateGroupRequest();
        request.setName("Graph Ninjas");
        request.setDescription("desc");
        request.setTags(List.of("graph"));

        GroupResponse response = groupService.createGroup(request);

        assertThat(response.getId()).isNotNull();
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void listGroupsSupportsPagination() {
        for (int i = 0; i < 3; i++) {
            CreateGroupRequest request = new CreateGroupRequest();
            request.setName("Group " + i);
            request.setDescription("desc");
            request.setTags(List.of("tag"));
            groupService.createGroup(request);
        }
        var page = groupService.listGroups(0, 2);

        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(3);
    }

    @Test
    void joinAndLeaveAdjustMemberCount() {
        CreateGroupRequest request = new CreateGroupRequest();
        request.setName("Graph Ninjas");
        request.setDescription("desc");
        request.setTags(List.of("graph"));
        GroupResponse created = groupService.createGroup(request);

        GroupResponse afterJoin = groupService.joinGroup(created.getId());
        assertThat(afterJoin.getMemberCount()).isEqualTo(1);

        GroupResponse afterLeave = groupService.leaveGroup(created.getId());
        assertThat(afterLeave.getMemberCount()).isEqualTo(0);
    }

    @Test
    void leaveGroupWithoutMembersThrows() {
        CreateGroupRequest request = new CreateGroupRequest();
        request.setName("Graph Ninjas");
        request.setDescription("desc");
        request.setTags(List.of("graph"));
        GroupResponse created = groupService.createGroup(request);

        assertThatThrownBy(() -> groupService.leaveGroup(created.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void getUnknownGroupThrows() {
        assertThatThrownBy(() -> groupService.getGroup(UUID.randomUUID()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
