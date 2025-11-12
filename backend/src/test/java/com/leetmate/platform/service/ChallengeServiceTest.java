package com.leetmate.platform.service;

import com.leetmate.platform.dto.challenge.ChallengeResponse;
import com.leetmate.platform.dto.challenge.CreateChallengeRequest;
import com.leetmate.platform.entity.Challenge;
import com.leetmate.platform.entity.ChallengeDifficulty;
import com.leetmate.platform.entity.StudyGroup;
import com.leetmate.platform.entity.User;
import com.leetmate.platform.entity.UserRole;
import com.leetmate.platform.exception.ResourceNotFoundException;
import com.leetmate.platform.repository.ChallengeRepository;
import com.leetmate.platform.repository.StudyGroupRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChallengeServiceTest {

    @Mock
    private ChallengeRepository challengeRepository;

    @Mock
    private StudyGroupRepository groupRepository;

    private ChallengeService challengeService;
    private StudyGroup group;

    @BeforeEach
    void setUp() {
        challengeService = new ChallengeService(challengeRepository, groupRepository);
        User mentor = new User(UUID.randomUUID(), "Mentor", "mentor@demo.com", "hash", UserRole.MENTOR, Instant.now());
        group = new StudyGroup(UUID.randomUUID(), mentor, "Graph Ninjas", "desc", List.of("graph"), Instant.now());
    }

    @Test
    void createChallengeRequiresExistingGroup() {
        UUID groupId = UUID.randomUUID();
        when(groupRepository.findById(groupId)).thenReturn(Optional.empty());
        CreateChallengeRequest request = buildRequest();

        assertThatThrownBy(() -> challengeService.createChallenge(groupId, request, UUID.randomUUID()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createAndListChallenges() {
        CreateChallengeRequest request = buildRequest();
        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));
        when(challengeRepository.save(any(Challenge.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(challengeRepository.findByGroup_IdOrderByCreatedAtDesc(group.getId()))
                .thenReturn(List.of(new Challenge(UUID.randomUUID(), group, "Two Sum", "desc",
                        "java", ChallengeDifficulty.EASY, "class Solution {}", Instant.now())));

        ChallengeResponse response = challengeService.createChallenge(group.getId(), request, group.getMentor().getId());
        assertThat(response.getDifficulty()).isEqualTo(ChallengeDifficulty.EASY);

        List<ChallengeResponse> list = challengeService.listGroupChallenges(group.getId());
        assertThat(list).hasSize(1);
    }

    @Test
    void getUnknownChallengeThrows() {
        when(challengeRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

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
}
