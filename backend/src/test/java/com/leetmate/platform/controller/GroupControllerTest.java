package com.leetmate.platform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leetmate.platform.dto.common.PageResponse;
import com.leetmate.platform.dto.group.CreateGroupRequest;
import com.leetmate.platform.dto.group.GroupMemberResponse;
import com.leetmate.platform.dto.group.GroupResponse;
import com.leetmate.platform.entity.UserRole;
import com.leetmate.platform.security.UserPrincipal;
import com.leetmate.platform.repository.UserRepository;
import com.leetmate.platform.security.JwtAuthenticationFilter;
import com.leetmate.platform.security.JwtService;
import com.leetmate.platform.service.GroupService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GroupController.class)
@AutoConfigureMockMvc(addFilters = false)
class GroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GroupService groupService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private final UserPrincipal mentorPrincipal =
            new UserPrincipal(UUID.randomUUID(), "mentor@demo.com", "password", UserRole.MENTOR);

    @Test
    void createGroupReturns201() throws Exception {
        GroupResponse response = new GroupResponse(UUID.randomUUID(), "Graph Ninjas", "desc",
                List.of("graph"), 0, Instant.now(), mentorPrincipal.getId(), "Mentor");
        when(groupService.createGroup(ArgumentMatchers.any(CreateGroupRequest.class), ArgumentMatchers.any(UUID.class)))
                .thenReturn(response);

        CreateGroupRequest request = new CreateGroupRequest();
        request.setName("Graph Ninjas");
        request.setDescription("desc");
        request.setTags(List.of("graph"));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mentorPrincipal, mentorPrincipal.getPassword(), mentorPrincipal.getAuthorities()));

        mockMvc.perform(post("/groups")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                        mentorPrincipal, mentorPrincipal.getPassword(), mentorPrincipal.getAuthorities())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Graph Ninjas"));

        SecurityContextHolder.clearContext();
    }

    @Test
    void createGroupValidationError() throws Exception {
        CreateGroupRequest request = new CreateGroupRequest();
        request.setName("");
        request.setDescription("");
        request.setTags(List.of());

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mentorPrincipal, mentorPrincipal.getPassword(), mentorPrincipal.getAuthorities()));

        mockMvc.perform(post("/groups")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                        mentorPrincipal, mentorPrincipal.getPassword(), mentorPrincipal.getAuthorities())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        SecurityContextHolder.clearContext();
    }

    @Test
    void listGroupsDelegatesToService() throws Exception {
        GroupResponse response = new GroupResponse(UUID.randomUUID(), "Graph Ninjas", "desc",
                List.of("graph"), 0, Instant.now(), mentorPrincipal.getId(), "Mentor");
        PageResponse<GroupResponse> page = new PageResponse<>(List.of(response), 0, 20, 1, 1);
        when(groupService.listGroups(0, 20)).thenReturn(page);

        mockMvc.perform(get("/groups?page=0&size=20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Graph Ninjas"));
    }

    @Test
    void listMenteesForGroupReturns200() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID menteeId = UUID.randomUUID();
        GroupMemberResponse memberResponse = new GroupMemberResponse(
                menteeId, "Test Mentee", "mentee@test.com", Instant.now());
        when(groupService.listMenteesForGroup(groupId)).thenReturn(List.of(memberResponse));

        mockMvc.perform(get("/groups/" + groupId + "/mentees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(menteeId.toString()))
                .andExpect(jsonPath("$[0].name").value("Test Mentee"))
                .andExpect(jsonPath("$[0].email").value("mentee@test.com"));
    }
}
