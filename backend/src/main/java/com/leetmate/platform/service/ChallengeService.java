package com.leetmate.platform.service;

import com.leetmate.platform.dto.challenge.ChallengeResponse;
import com.leetmate.platform.dto.challenge.CreateChallengeRequest;
import com.leetmate.platform.entity.Challenge;
import com.leetmate.platform.entity.ChallengeDifficulty;
import com.leetmate.platform.exception.ResourceNotFoundException;
import com.leetmate.platform.repository.ChallengeRepository;
import com.leetmate.platform.repository.StudyGroupRepository;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Handles business logic for challenges.
 */
@Service
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final StudyGroupRepository studyGroupRepository;

    /**
     * Creates a new service instance.
     *
     * @param challengeRepository challenge repo
     * @param studyGroupRepository group repo
     */
    public ChallengeService(ChallengeRepository challengeRepository,
                            StudyGroupRepository studyGroupRepository) {
        this.challengeRepository = challengeRepository;
        this.studyGroupRepository = studyGroupRepository;
    }

    /**
     * Creates a challenge under a group.
     *
     * @param groupId group identifier
     * @param request payload
     * @return created challenge
     */
    public ChallengeResponse createChallenge(UUID groupId, CreateChallengeRequest request) {
        ensureGroupExists(groupId);
        ChallengeDifficulty difficulty;
        try {
            difficulty = ChallengeDifficulty
                    .valueOf(request.getDifficulty().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("difficulty must be EASY, MEDIUM or HARD");
        }
        Challenge challenge = new Challenge(UUID.randomUUID(), groupId, request.getTitle(),
                request.getDescription(), request.getLanguage().toLowerCase(Locale.ROOT),
                difficulty, request.getStarterCode(), Instant.now());
        challengeRepository.save(challenge);
        return toResponse(challenge);
    }

    /**
     * Lists challenges for a group.
     *
     * @param groupId group identifier
     * @return list response
     */
    public List<ChallengeResponse> listGroupChallenges(UUID groupId) {
        ensureGroupExists(groupId);
        return challengeRepository.findByGroupId(groupId).stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Retrieves a challenge by identifier.
     *
     * @param challengeId identifier
     * @return representation
     */
    public ChallengeResponse getChallenge(UUID challengeId) {
        return toResponse(findChallenge(challengeId));
    }

    /**
     * Ensures the challenge exists and returns the entity.
     *
     * @param challengeId identifier
     * @return entity
     */
    public Challenge findChallenge(UUID challengeId) {
        return challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ResourceNotFoundException("Challenge %s not found".formatted(challengeId)));
    }

    private ChallengeResponse toResponse(Challenge challenge) {
        return new ChallengeResponse(challenge.getId(), challenge.getGroupId(), challenge.getTitle(),
                challenge.getDescription(), challenge.getLanguage(), challenge.getDifficulty(),
                challenge.getStarterCode(), challenge.getCreatedAt());
    }

    private void ensureGroupExists(UUID groupId) {
        studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group %s not found".formatted(groupId)));
    }
}
