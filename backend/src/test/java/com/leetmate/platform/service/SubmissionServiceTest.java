package com.leetmate.platform.service;

import com.leetmate.platform.ai.MockAiReviewProvider;
import com.leetmate.platform.dto.challenge.CreateChallengeRequest;
import com.leetmate.platform.dto.group.CreateGroupRequest;
import com.leetmate.platform.dto.submission.SubmissionResponse;
import com.leetmate.platform.dto.submission.SubmitSolutionRequest;
import com.leetmate.platform.exception.ResourceNotFoundException;
import com.leetmate.platform.repository.ChallengeRepository;
import com.leetmate.platform.repository.StudyGroupRepository;
import com.leetmate.platform.repository.SubmissionRepository;
import com.leetmate.platform.util.CyclomaticComplexityCalculator;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SubmissionServiceTest {

    private SubmissionService submissionService;
    private ChallengeService challengeService;
    private UUID groupId;
    private UUID challengeId;

    @BeforeEach
    void setUp() {
        StudyGroupRepository groupRepository = new StudyGroupRepository();
        GroupService groupService = new GroupService(groupRepository);
        groupId = createGroup(groupService);
        ChallengeRepository challengeRepository = new ChallengeRepository();
        challengeService = new ChallengeService(challengeRepository, groupRepository);
        challengeId = createChallenge(challengeService);
        submissionService = new SubmissionService(new SubmissionRepository(), challengeService,
                new MockAiReviewProvider(), new CyclomaticComplexityCalculator());
    }

    @Test
    void submitCreatesReviewAndAwardsCredit() {
        SubmissionResponse response = submissionService.submit(challengeId, newSubmitRequest());

        assertThat(response.getCreditsAwarded()).isEqualTo(1);
        assertThat(response.getReview()).isNotNull();
        assertThat(response.getReview().getComplexity()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void listSubmissionsSupportsPagination() {
        submissionService.submit(challengeId, newSubmitRequest());
        submissionService.submit(challengeId, newSubmitRequest());

        var page = submissionService.listSubmissions(challengeId, 0, 1);
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getTotalElements()).isEqualTo(2);
    }

    @Test
    void getUnknownSubmissionThrows() {
        assertThatThrownBy(() -> submissionService.getSubmission(UUID.randomUUID()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private UUID createGroup(GroupService groupService) {
        CreateGroupRequest request = new CreateGroupRequest();
        request.setName("Graph Ninjas");
        request.setDescription("desc");
        request.setTags(List.of("graph"));
        return groupService.createGroup(request).getId();
    }

    private UUID createChallenge(ChallengeService challengeService) {
        CreateChallengeRequest request = new CreateChallengeRequest();
        request.setTitle("Two Sum");
        request.setDescription("desc");
        request.setLanguage("java");
        request.setDifficulty("easy");
        request.setStarterCode("class Solution {}");
        return challengeService.createChallenge(groupId, request).getId();
    }

    private SubmitSolutionRequest newSubmitRequest() {
        SubmitSolutionRequest request = new SubmitSolutionRequest();
        request.setLanguage("java");
        request.setCode("""
                public class Solution {
                    public int add(int a, int b) {
                        if (a > 0 && b > 0) {
                            return a + b;
                        }
                        return 0;
                    }
                }
                """);
        return request;
    }
}
