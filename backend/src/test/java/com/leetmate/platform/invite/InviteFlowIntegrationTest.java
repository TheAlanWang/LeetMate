package com.leetmate.platform.invite;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leetmate.platform.group.CreateGroupRequest;
import com.leetmate.platform.group.GroupResponse;
import com.leetmate.platform.group.StudyGroup;
import com.leetmate.platform.group.StudyGroupRepository;
import com.leetmate.platform.membership.MembershipRepository;
import com.leetmate.platform.user.User;
import com.leetmate.platform.user.UserRepository;
import com.leetmate.platform.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class InviteFlowIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    MentorInviteRepository mentorInviteRepository;

    @Autowired
    MembershipRepository membershipRepository;

    @Autowired
    StudyGroupRepository groupRepository;

    private User mentor;
    private User mentee;
    private StudyGroup group;

    @BeforeEach
    void setupData() {
        membershipRepository.deleteAll();
        mentorInviteRepository.deleteAll();
        groupRepository.deleteAll();
        userRepository.deleteAll();

        mentor = createUser("Mentor X", "mentorx@example.com", UserRole.MENTOR);
        mentee = createUser("Mentee X", "menteex@example.com", UserRole.MENTEE);
    }

    @Test
    void acceptingInviteCreatesMembership() throws Exception {
        group = createGroup();
        InviteResponse invite = sendInvite(mentor.getId(), mentee.getId());

        InviteDecisionRequest accept = new InviteDecisionRequest(InviteStatus.ACCEPTED);
        mockMvc.perform(patch("/invites/{id}", invite.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(accept)))
                .andExpect(status().isOk());

        assertThat(membershipRepository.findByGroupAndUser(group, mentee)).isPresent();
    }

    @Test
    void nonOwningMentorCannotSendInvite() throws Exception {
        group = createGroup();
        User outsider = createUser("Mentor Y", "mentory@example.com", UserRole.MENTOR);
        String payload = objectMapper.writeValueAsString(new SendInviteRequest(
                outsider.getId(),
                mentee.getId()
        ));
        mockMvc.perform(post("/groups/{id}/invite", group.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    private InviteResponse sendInvite(Long mentorId, Long menteeId) throws Exception {
        String payload = objectMapper.writeValueAsString(new SendInviteRequest(mentorId, menteeId));
        String content = mockMvc.perform(post("/groups/{id}/invite", group.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readValue(content, InviteResponse.class);
    }

    private StudyGroup createGroup() throws Exception {
        CreateGroupRequest request = new CreateGroupRequest(
                "Graph Theory Squad",
                "Graphs",
                "Intermediate",
                mentor.getId(),
                "Weekly graph pair programming",
                15
        );
        String response = mockMvc.perform(post("/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        GroupResponse groupResponse = objectMapper.readValue(response, GroupResponse.class);
        return groupRepository.findById(groupResponse.id()).orElseThrow();
    }

    private User createUser(String name, String email, UserRole role) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setRole(role);
        return userRepository.save(user);
    }
}
