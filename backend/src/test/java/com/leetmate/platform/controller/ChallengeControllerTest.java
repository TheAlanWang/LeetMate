package com.leetmate.platform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leetmate.platform.dto.challenge.ChallengeResponse;
import com.leetmate.platform.dto.challenge.CreateChallengeRequest;
import com.leetmate.platform.entity.ChallengeDifficulty;
import com.leetmate.platform.service.ChallengeService;
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

@WebMvcTest(ChallengeController.class)
class ChallengeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ChallengeService challengeService;

    @Test
    void createChallengeReturns201() throws Exception {
        UUID groupId = UUID.randomUUID();
        ChallengeResponse response = new ChallengeResponse(UUID.randomUUID(), groupId, "Two Sum",
                "desc", "java", ChallengeDifficulty.EASY, "class Solution {}", Instant.now());
        when(challengeService.createChallenge(ArgumentMatchers.eq(groupId),
                ArgumentMatchers.any(CreateChallengeRequest.class))).thenReturn(response);

        CreateChallengeRequest request = new CreateChallengeRequest();
        request.setTitle("Two Sum");
        request.setDescription("desc");
        request.setLanguage("java");
        request.setDifficulty("EASY");
        request.setStarterCode("class Solution {}");

        mockMvc.perform(post("/groups/" + groupId + "/challenges")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Two Sum"));
    }

    @Test
    void listChallengesReturnsData() throws Exception {
        UUID groupId = UUID.randomUUID();
        ChallengeResponse response = new ChallengeResponse(UUID.randomUUID(), groupId, "Two Sum",
                "desc", "java", ChallengeDifficulty.EASY, "class Solution {}", Instant.now());
        when(challengeService.listGroupChallenges(groupId)).thenReturn(List.of(response));

        mockMvc.perform(get("/groups/" + groupId + "/challenges"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Two Sum"));
    }
}
