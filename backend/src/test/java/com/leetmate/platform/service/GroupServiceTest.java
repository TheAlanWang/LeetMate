package com.leetmate.platform.service;

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
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @Mock
    private StudyGroupRepository repository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GroupMemberRepository groupMemberRepository;

    private GroupService groupService;

    private User mentor;
    private User mentee;

    @BeforeEach
    void setUp() {
        groupService = new GroupService(repository, userRepository, groupMemberRepository);
        mentor = new User(UUID.randomUUID(), "Mentor", "mentor@demo.com", "hash", UserRole.MENTOR, Instant.now());
        mentee = new User(UUID.randomUUID(), "Mentee", "mentee@demo.com", "hash", UserRole.MENTEE, Instant.now());
    }

    @Test
    void createGroupPersistsAndReturnsResponse() {
        CreateGroupRequest request = new CreateGroupRequest();
        request.setName("Graph Ninjas");
        request.setDescription("desc");
        request.setTags(List.of("graph"));
        when(userRepository.findById(mentor.getId())).thenReturn(Optional.of(mentor));
        when(repository.save(any(StudyGroup.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GroupResponse response = groupService.createGroup(request, mentor.getId());

        assertThat(response.getMentorId()).isEqualTo(mentor.getId());
        ArgumentCaptor<StudyGroup> captor = ArgumentCaptor.forClass(StudyGroup.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Graph Ninjas");
    }

    @Test
    void listGroupsSupportsPagination() {
        StudyGroup group = new StudyGroup(UUID.randomUUID(), mentor, "Group 1", "desc", List.of("tag"), Instant.now());
        when(repository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, 2)))
                .thenReturn(new PageImpl<>(List.of(group), PageRequest.of(0, 2), 1));

        var page = groupService.listGroups(0, 2);

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getTotalElements()).isEqualTo(1);
    }

    @Test
    void joinAndLeaveAdjustMemberCount() {
        StudyGroup group = new StudyGroup(UUID.randomUUID(), mentor, "Group", "desc", List.of("tag"), Instant.now());
        when(repository.findById(group.getId())).thenReturn(Optional.of(group));
        when(repository.save(group)).thenReturn(group);
        when(userRepository.findById(mentee.getId())).thenReturn(Optional.of(mentee));
        when(groupMemberRepository.existsByGroupIdAndMemberId(group.getId(), mentee.getId())).thenReturn(false);
        when(groupMemberRepository.findByGroupIdAndMemberId(group.getId(), mentee.getId()))
                .thenReturn(Optional.of(new GroupMember(UUID.randomUUID(), group, mentee, Instant.now())));

        GroupResponse afterJoin = groupService.joinGroup(group.getId(), mentee.getId());
        assertThat(afterJoin.getMemberCount()).isEqualTo(1);

        GroupResponse afterLeave = groupService.leaveGroup(group.getId(), mentee.getId());
        assertThat(afterLeave.getMemberCount()).isEqualTo(0);
    }

    @Test
    void leaveGroupWithoutMembersThrows() {
        StudyGroup group = new StudyGroup(UUID.randomUUID(), mentor, "Group", "desc", List.of("tag"), Instant.now());
        when(repository.findById(group.getId())).thenReturn(Optional.of(group));
        when(userRepository.findById(mentee.getId())).thenReturn(Optional.of(mentee));
        when(groupMemberRepository.findByGroupIdAndMemberId(group.getId(), mentee.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> groupService.leaveGroup(group.getId(), mentee.getId()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getUnknownGroupThrows() {
        UUID groupId = UUID.randomUUID();
        when(repository.findById(groupId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> groupService.getGroup(groupId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
