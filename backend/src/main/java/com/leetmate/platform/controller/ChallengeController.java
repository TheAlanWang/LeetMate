package com.leetmate.platform.controller;

import com.leetmate.platform.dto.challenge.ChallengeResponse;
import com.leetmate.platform.dto.challenge.CreateChallengeRequest;
import com.leetmate.platform.service.ChallengeService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Challenge REST controller.
 */
@RestController
@Validated
public class ChallengeController {

    private final ChallengeService challengeService;

    /**
     * Creates the controller.
     *
     * @param challengeService service dependency
     */
    public ChallengeController(ChallengeService challengeService) {
        this.challengeService = challengeService;
    }

    /**
     * Creates a new challenge underneath a group.
     *
     * @param groupId group identifier
     * @param request payload
     * @return created challenge
     */
    @PostMapping("/groups/{groupId}/challenges")
    @ResponseStatus(HttpStatus.CREATED)
    public ChallengeResponse createChallenge(@PathVariable UUID groupId,
                                             @Valid @RequestBody CreateChallengeRequest request) {
        return challengeService.createChallenge(groupId, request);
    }

    /**
     * Lists challenges for a group.
     *
     * @param groupId group identifier
     * @return list response
     */
    @GetMapping("/groups/{groupId}/challenges")
    public List<ChallengeResponse> listChallenges(@PathVariable UUID groupId) {
        return challengeService.listGroupChallenges(groupId);
    }

    /**
     * Returns challenge details.
     *
     * @param challengeId identifier
     * @return response
     */
    @GetMapping("/challenges/{challengeId}")
    public ChallengeResponse getChallenge(@PathVariable UUID challengeId) {
        return challengeService.getChallenge(challengeId);
    }
}
