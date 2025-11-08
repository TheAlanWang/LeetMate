package com.leetmate.platform.application;

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
class ApplicationFlowIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    MembershipRepository membershipRepository;

    @Autowired
    GroupApplicationRepository applicationRepository;

    @Autowired
    StudyGroupRepository groupRepository;

    private User mentor;
    private User mentee;

    @BeforeEach
    void setupUsers() {
        membershipRepository.deleteAll();
        applicationRepository.deleteAll();
        groupRepository.deleteAll();
        userRepository.deleteAll();

        mentor = new User();
        mentor.setName("Mentor One");
        mentor.setEmail("mentor@example.com");
        mentor.setRole(UserRole.MENTOR);
        mentor = userRepository.save(mentor);

        mentee = new User();
        mentee.setName("Mentee One");
        mentee.setEmail("mentee@example.com");
        mentee.setRole(UserRole.MENTEE);
        mentee = userRepository.save(mentee);
    }

    @Test
    void menteeApplicationGetsApprovedAndCreatesMembership() throws Exception {
        Long groupId = createGroup();

        StudyGroup group = groupRepository.findById(groupId).orElseThrow();
        ApplicationResponse response = submitApplication(groupId);

        UpdateApplicationStatusRequest approve = new UpdateApplicationStatusRequest(ApplicationStatus.APPROVED);
        mockMvc.perform(patch("/applications/{id}", response.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approve)))
                .andExpect(status().isOk());

        assertThat(membershipRepository.findByGroupAndUser(group, mentee)).isPresent();
    }

    @Test
    void duplicateApplicationsAreRejected() throws Exception {
        Long groupId = createGroup();
        submitApplication(groupId);

        mockMvc.perform(post("/groups/{id}/apply", groupId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SubmitApplicationRequest(
                                mentee.getId(),
                                "Second try",
                                "Mid",
                                "Weekends"
                        ))))
                .andExpect(status().isBadRequest());
    }

    private ApplicationResponse submitApplication(Long groupId) throws Exception {
        String payload = objectMapper.writeValueAsString(new SubmitApplicationRequest(
                mentee.getId(),
                "Ready to grind",
                "Senior",
                "Evenings"
        ));

        String content = mockMvc.perform(post("/groups/{id}/apply", groupId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readValue(content, ApplicationResponse.class);
    }

    private Long createGroup() throws Exception {
        CreateGroupRequest request = new CreateGroupRequest(
                "Dynamic Programming Club",
                "DP",
                "Advanced",
                mentor.getId(),
                "Daily DP drills",
                10
        );
        String response = mockMvc.perform(post("/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        GroupResponse group = objectMapper.readValue(response, GroupResponse.class);
        return group.id();
    }
}
