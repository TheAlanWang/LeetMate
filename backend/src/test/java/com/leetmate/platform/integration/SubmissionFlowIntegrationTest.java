package com.leetmate.platform.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class SubmissionFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void endToEndFlowCreatesReviewAndAwardsCredit() throws Exception {
        String mentorToken = registerUser("""
                {
                  "name": "Mentor One",
                  "email": "mentor@test.com",
                  "password": "secret123",
                  "role": "MENTOR"
                }
                """);
        String menteeToken = registerUser("""
                {
                  "name": "Mentee One",
                  "email": "mentee@test.com",
                  "password": "secret123",
                  "role": "MENTEE"
                }
                """);

        String groupPayload = objectMapper.writeValueAsString(
                new Payload("Graph Ninjas", "desc", List.of("graph")));
        MvcResult groupResult = mockMvc.perform(post("/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + mentorToken)
                        .content(groupPayload))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode groupJson = objectMapper.readTree(groupResult.getResponse().getContentAsString());
        String groupId = groupJson.get("id").asText();

        String challengePayload = """
                {
                  "title": "Two Sum",
                  "description": "Find indices",
                  "language": "java",
                  "difficulty": "EASY",
                  "starterCode": "class Solution {}"
                }
                """;
        MvcResult challengeResult = mockMvc.perform(post("/groups/" + groupId + "/challenges")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + mentorToken)
                        .content(challengePayload))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode challengeJson = objectMapper.readTree(challengeResult.getResponse().getContentAsString());
        String challengeId = challengeJson.get("id").asText();

        String submissionPayload = """
                {
                  "language": "java",
                  "code": "class Solution { int twoSum(int[] nums) { if(nums.length < 2){return 0;} return 1; } }"
                }
                """;
        MvcResult submissionResult = mockMvc.perform(post("/challenges/" + challengeId + "/submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + menteeToken)
                        .content(submissionPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.review.summary").exists())
                .andExpect(jsonPath("$.creditsAwarded").value(1))
                .andReturn();
        JsonNode submissionJson = objectMapper.readTree(submissionResult.getResponse().getContentAsString());
        String submissionId = submissionJson.get("id").asText();

        mockMvc.perform(get("/submissions/" + submissionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.review.complexity").value(2));

        mockMvc.perform(get("/challenges/" + challengeId + "/submissions?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(submissionId));
    }

    private String registerUser(String payload) throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("token").asText();
    }

    record Payload(String name, String description, List<String> tags) {
    }
}
