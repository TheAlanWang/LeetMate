package com.leetmate.platform.service;

import com.leetmate.platform.ai.MockAiReviewProvider;
import com.leetmate.platform.dto.submission.SubmissionResponse;
import com.leetmate.platform.dto.submission.SubmitSolutionRequest;
import com.leetmate.platform.entity.Challenge;
import com.leetmate.platform.entity.ChallengeDifficulty;
import com.leetmate.platform.entity.StudyGroup;
import com.leetmate.platform.entity.Submission;
import com.leetmate.platform.entity.User;
import com.leetmate.platform.entity.UserRole;
import com.leetmate.platform.exception.ResourceNotFoundException;
import com.leetmate.platform.repository.SubmissionRepository;
import com.leetmate.platform.repository.UserRepository;
import com.leetmate.platform.util.CyclomaticComplexityCalculator;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubmissionServiceTest {

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private ChallengeService challengeService;

    @Mock
    private UserRepository userRepository;

    private SubmissionService submissionService;
    private Challenge challenge;
    private User mentee;

    @BeforeEach
    void setUp() {
        submissionService = new SubmissionService(submissionRepository, challengeService,
                userRepository, new MockAiReviewProvider(), new CyclomaticComplexityCalculator());
        User mentor = new User(UUID.randomUUID(), "Mentor", "mentor@demo.com", "hash", UserRole.MENTOR, Instant.now());
        StudyGroup group = new StudyGroup(UUID.randomUUID(), mentor, "Graph", "desc", List.of("graph"), Instant.now());
        challenge = new Challenge(UUID.randomUUID(), group, "Two Sum", "desc", "java",
                ChallengeDifficulty.EASY, "class Solution {}", Instant.now());
        mentee = new User(UUID.randomUUID(), "Mentee", "mentee@demo.com", "hash", UserRole.MENTEE, Instant.now());
    }

    @Test
    void submitCreatesReviewAndAwardsCredit() {
        when(challengeService.findChallenge(challenge.getId())).thenReturn(challenge);
        when(userRepository.findById(mentee.getId())).thenReturn(Optional.of(mentee));
        when(submissionRepository.save(any(Submission.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SubmissionResponse response = submissionService.submit(challenge.getId(), newSubmitRequest(), mentee.getId());

        assertThat(response.getCreditsAwarded()).isEqualTo(1);
        assertThat(response.getReview()).isNotNull();
    }

    @Test
    void listSubmissionsSupportsPagination() {
        when(challengeService.findChallenge(challenge.getId())).thenReturn(challenge);
        Submission persisted = new Submission(UUID.randomUUID(), challenge, mentee, "java", "code", 1, Instant.now());
        when(submissionRepository.findByChallenge_IdOrderByCreatedAtDesc(eq(challenge.getId()), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(persisted), PageRequest.of(0, 1), 1));

        var page = submissionService.listSubmissions(challenge.getId(), 0, 1);

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getTotalElements()).isEqualTo(1);
    }

    @Test
    void getUnknownSubmissionThrows() {
        when(submissionRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> submissionService.getSubmission(UUID.randomUUID()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private SubmitSolutionRequest newSubmitRequest() {
        SubmitSolutionRequest request = new SubmitSolutionRequest();
        request.setLanguage("java");
        request.setCode("class Solution {}");
        return request;
    }
}
