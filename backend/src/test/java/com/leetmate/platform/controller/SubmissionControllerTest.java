package com.leetmate.platform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leetmate.platform.dto.common.PageResponse;
import com.leetmate.platform.dto.submission.ReviewResponse;
import com.leetmate.platform.dto.submission.SubmissionResponse;
import com.leetmate.platform.dto.submission.SubmitSolutionRequest;
import com.leetmate.platform.entity.UserRole;
import com.leetmate.platform.security.UserPrincipal;
import com.leetmate.platform.service.SubmissionService;
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
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SubmissionController.class)
@AutoConfigureMockMvc(addFilters = false)
class SubmissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SubmissionService submissionService;

    private final UserPrincipal menteePrincipal =
            new UserPrincipal(UUID.randomUUID(), "mentee@demo.com", "pwd", UserRole.MENTEE);

    @Test
    void submitReturns201() throws Exception {
        UUID challengeId = UUID.randomUUID();
        SubmissionResponse response = new SubmissionResponse(UUID.randomUUID(), challengeId,
                menteePrincipal.getId(), "Mentee", "java",
                "class Solution {}", 1, Instant.now(),
                new ReviewResponse(UUID.randomUUID(), "Summary", 5, List.of("Tip"), Instant.now()));
        when(submissionService.submit(ArgumentMatchers.eq(challengeId),
                ArgumentMatchers.any(SubmitSolutionRequest.class),
                ArgumentMatchers.any(UUID.class))).thenReturn(response);

        SubmitSolutionRequest request = new SubmitSolutionRequest();
        request.setLanguage("java");
        request.setCode("class Solution {}");

        mockMvc.perform(post("/challenges/" + challengeId + "/submissions")
                        .with(SecurityMockMvcRequestPostProcessors.user(menteePrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.review.summary").value("Summary"));
    }

    @Test
    void listSubmissionsReturnsPagination() throws Exception {
        UUID challengeId = UUID.randomUUID();
        SubmissionResponse response = new SubmissionResponse(UUID.randomUUID(), challengeId,
                menteePrincipal.getId(), "Mentee", "java",
                "code", 1, Instant.now(),
                new ReviewResponse(UUID.randomUUID(), "Summary", 5, List.of("Tip"), Instant.now()));
        PageResponse<SubmissionResponse> page = new PageResponse<>(List.of(response), 0, 20, 1, 1);
        when(submissionService.listSubmissions(challengeId, 0, 20)).thenReturn(page);

        mockMvc.perform(get("/challenges/" + challengeId + "/submissions?page=0&size=20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].language").value("java"));
    }
}
