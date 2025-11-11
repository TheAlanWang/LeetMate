package com.leetmate.platform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leetmate.platform.dto.common.PageResponse;
import com.leetmate.platform.dto.group.CreateGroupRequest;
import com.leetmate.platform.dto.group.GroupResponse;
import com.leetmate.platform.service.GroupService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GroupController.class)
class GroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GroupService groupService;

    @Test
    void createGroupReturns201() throws Exception {
        GroupResponse response = new GroupResponse(UUID.randomUUID(), "Graph Ninjas", "desc",
                List.of("graph"), 0, Instant.now());
        when(groupService.createGroup(ArgumentMatchers.any(CreateGroupRequest.class)))
                .thenReturn(response);

        CreateGroupRequest request = new CreateGroupRequest();
        request.setName("Graph Ninjas");
        request.setDescription("desc");
        request.setTags(List.of("graph"));

        mockMvc.perform(post("/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Graph Ninjas"));
    }

    @Test
    void createGroupValidationError() throws Exception {
        CreateGroupRequest request = new CreateGroupRequest();
        request.setName("");
        request.setDescription("");
        request.setTags(List.of());

        mockMvc.perform(post("/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listGroupsDelegatesToService() throws Exception {
        GroupResponse response = new GroupResponse(UUID.randomUUID(), "Graph Ninjas", "desc",
                List.of("graph"), 0, Instant.now());
        PageResponse<GroupResponse> page = new PageResponse<>(List.of(response), 0, 20, 1, 1);
        when(groupService.listGroups(0, 20)).thenReturn(page);

        mockMvc.perform(get("/groups?page=0&size=20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Graph Ninjas"));
    }
}
