package com.leetmate.platform.service;

import com.leetmate.platform.dto.challenge.ChallengeResponse;
import com.leetmate.platform.dto.challenge.CreateChallengeRequest;
import com.leetmate.platform.entity.ChallengeDifficulty;
import com.leetmate.platform.exception.ResourceNotFoundException;
import com.leetmate.platform.repository.ChallengeRepository;
import com.leetmate.platform.repository.StudyGroupRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChallengeServiceTest {

    private ChallengeService challengeService;
    private StudyGroupRepository groupRepository;

    @BeforeEach
    void setUp() {
        groupRepository = new StudyGroupRepository();
        challengeService = new ChallengeService(new ChallengeRepository(), groupRepository);
    }

    @Test
    void createChallengeRequiresExistingGroup() {
        UUID groupId = UUID.randomUUID();
        CreateChallengeRequest request = buildRequest();

        assertThatThrownBy(() -> challengeService.createChallenge(groupId, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createAndListChallenges() {
        UUID groupId = seedGroup();
        CreateChallengeRequest request = buildRequest();
        ChallengeResponse response = challengeService.createChallenge(groupId, request);

        assertThat(response.getDifficulty()).isEqualTo(ChallengeDifficulty.EASY);

        List<ChallengeResponse> list = challengeService.listGroupChallenges(groupId);
        assertThat(list).hasSize(1);
    }

    @Test
    void getUnknownChallengeThrows() {
        assertThatThrownBy(() -> challengeService.getChallenge(UUID.randomUUID()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private CreateChallengeRequest buildRequest() {
        CreateChallengeRequest request = new CreateChallengeRequest();
        request.setTitle("Two Sum");
        request.setDescription("desc");
        request.setLanguage("java");
        request.setDifficulty("easy");
        request.setStarterCode("class Solution {}");
        return request;
    }

    private UUID seedGroup() {
        var request = new com.leetmate.platform.dto.group.CreateGroupRequest();
        request.setName("Graph");
        request.setDescription("desc");
        request.setTags(List.of("tag"));
        var service = new GroupService(groupRepository);
        return service.createGroup(request).getId();
    }
}
