package com.leetmate.platform.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
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
class GroupChatIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void threadLifecycle_allowsMembersAndBlocksNonMembers() throws Exception {
        AuthResponse mentor = registerUser("""
                {"name":"Mentor","email":"mentor@test.com","password":"secret123","role":"MENTOR"}
                """);
        AuthResponse mentee = registerUser("""
                {"name":"Mentee","email":"mentee@test.com","password":"secret123","role":"MENTEE"}
                """);
        AuthResponse outsider = registerUser("""
                {"name":"Outsider","email":"outsider@test.com","password":"secret123","role":"MENTEE"}
                """);

        String groupId = createGroup(mentor.token());
        joinGroup(groupId, mentee.token());

        String threadId = createThread(groupId, mentee.token(), """
                {"title":"Daily","description":"desc","initialMessage":"hello","codeLanguage":"java"}
                """);

        MvcResult listResult = mockMvc.perform(get("/groups/" + groupId + "/threads?page=0&size=5")
                        .header("Authorization", "Bearer " + mentee.token()))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode listJson = objectMapper.readTree(listResult.getResponse().getContentAsString());
        assertThat(listJson.get("content")).hasSize(1);
        assertThat(listJson.get("content").get(0).get("id").asText()).isEqualTo(threadId);

        mockMvc.perform(get("/threads/" + threadId)
                        .header("Authorization", "Bearer " + mentee.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groupId").value(groupId));

        mockMvc.perform(get("/groups/" + groupId + "/threads?page=0&size=5")
                        .header("Authorization", "Bearer " + outsider.token()))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/threads/" + threadId)
                        .header("Authorization", "Bearer " + outsider.token()))
                .andExpect(status().isForbidden());
    }

    @Test
    void messages_supportRepliesAndOrdering() throws Exception {
        AuthResponse mentor = registerUser("""
                {"name":"Mentor","email":"mentor@test.com","password":"secret123","role":"MENTOR"}
                """);
        AuthResponse mentee = registerUser("""
                {"name":"Mentee","email":"mentee@test.com","password":"secret123","role":"MENTEE"}
                """);
        String groupId = createGroup(mentor.token());
        joinGroup(groupId, mentee.token());
        String threadId = createThread(groupId, mentee.token(), """
                {"title":"Thread","description":"desc"}
                """);

        String messageId = postMessage(threadId, mentee.token(), """
                {"content":"top-level","codeLanguage":"python"}
                """);
        String replyId = postMessage(threadId, mentor.token(), """
                {"content":"reply","parentMessageId":"%s"}
                """.formatted(messageId));

        MvcResult listResult = mockMvc.perform(get("/threads/" + threadId + "/messages?page=0&size=10")
                        .header("Authorization", "Bearer " + mentee.token()))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode listJson = objectMapper.readTree(listResult.getResponse().getContentAsString());

        assertThat(listJson.get("content")).hasSize(2);
        assertThat(listJson.get("content").get(0).get("id").asText()).isEqualTo(messageId);
        assertThat(listJson.get("content").get(1).get("id").asText()).isEqualTo(replyId);
        assertThat(listJson.get("content").get(1).get("parentMessageId").asText()).isEqualTo(messageId);
    }

    @Test
    void replies_validateParentOwnershipAndExistence() throws Exception {
        AuthResponse mentor = registerUser("""
                {"name":"Mentor","email":"mentor@test.com","password":"secret123","role":"MENTOR"}
                """);
        AuthResponse mentee = registerUser("""
                {"name":"Mentee","email":"mentee@test.com","password":"secret123","role":"MENTEE"}
                """);
        String groupId = createGroup(mentor.token());
        joinGroup(groupId, mentee.token());

        String threadA = createThread(groupId, mentee.token(), """
                {"title":"A","description":"desc"}
                """);
        String threadB = createThread(groupId, mentor.token(), """
                {"title":"B","description":"desc"}
                """);

        String parentInB = postMessage(threadB, mentor.token(), """
                {"content":"parent-B"}
                """);

        // Parent from another thread -> 403
        mockMvc.perform(post("/threads/" + threadA + "/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + mentee.token())
                        .content("""
                                {"content":"wrong parent","parentMessageId":"%s"}
                                """.formatted(parentInB)))
                .andExpect(status().isForbidden());

        // Missing parent -> 404
        mockMvc.perform(post("/threads/" + threadA + "/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + mentee.token())
                        .content("""
                                {"content":"missing parent","parentMessageId":"%s"}
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isNotFound());
    }

    @Test
    void nonMembersCannotPostOrListMessages() throws Exception {
        AuthResponse mentor = registerUser("""
                {"name":"Mentor","email":"mentor@test.com","password":"secret123","role":"MENTOR"}
                """);
        AuthResponse outsider = registerUser("""
                {"name":"Outsider","email":"outsider@test.com","password":"secret123","role":"MENTEE"}
                """);

        String groupId = createGroup(mentor.token());
        String threadId = createThread(groupId, mentor.token(), """
                {"title":"T","description":"desc"}
                """);

        mockMvc.perform(post("/threads/" + threadId + "/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + outsider.token())
                        .content("""
                                {"content":"not allowed"}
                                """))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/threads/" + threadId + "/messages?page=0&size=5")
                        .header("Authorization", "Bearer " + outsider.token()))
                .andExpect(status().isForbidden());
    }

    private AuthResponse registerUser(String payload) throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return new AuthResponse(json.get("token").asText(), json.get("user").get("id").asText());
    }

    private String createGroup(String mentorToken) throws Exception {
        String payload = objectMapper.writeValueAsString(new GroupPayload("Group", "Desc", List.of("tag")));
        MvcResult result = mockMvc.perform(post("/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + mentorToken)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("id").asText();
    }

    private void joinGroup(String groupId, String menteeToken) throws Exception {
        mockMvc.perform(post("/groups/" + groupId + "/join")
                        .header("Authorization", "Bearer " + menteeToken))
                .andExpect(status().isOk());
    }

    private String createThread(String groupId, String token, String payload) throws Exception {
        MvcResult result = mockMvc.perform(post("/groups/" + groupId + "/threads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("id").asText();
    }

    private String postMessage(String threadId, String token, String payload) throws Exception {
        MvcResult result = mockMvc.perform(post("/threads/" + threadId + "/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("id").asText();
    }

    record AuthResponse(String token, String userId) {
    }

    record GroupPayload(String name, String description, List<String> tags) {
    }
}
