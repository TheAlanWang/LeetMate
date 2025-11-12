package com.leetmate.platform.controller;

import com.leetmate.platform.dto.common.PageResponse;
import com.leetmate.platform.dto.submission.SubmissionResponse;
import com.leetmate.platform.dto.submission.SubmitSolutionRequest;
import com.leetmate.platform.security.UserPrincipal;
import com.leetmate.platform.service.SubmissionService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Handles submission endpoints including AI review integration.
 */
@RestController
@Validated
public class SubmissionController {

    private final SubmissionService submissionService;

    /**
     * Creates a new controller.
     *
     * @param submissionService service dependency
     */
    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    /**
     * Submits code for review.
     *
     * @param challengeId challenge identifier
     * @param request     payload
     * @return submission response
     */
    @PostMapping("/challenges/{challengeId}/submissions")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('MENTEE')")
    public SubmissionResponse submit(@PathVariable UUID challengeId,
                                     @AuthenticationPrincipal UserPrincipal principal,
                                     @Valid @RequestBody SubmitSolutionRequest request) {
        return submissionService.submit(challengeId, request, principal.getId());
    }

    /**
     * Retrieves submission details.
     *
     * @param submissionId identifier
     * @return response
     */
    @GetMapping("/submissions/{submissionId}")
    public SubmissionResponse getSubmission(@PathVariable UUID submissionId) {
        return submissionService.getSubmission(submissionId);
    }

    /**
     * Lists submissions for a challenge.
     *
     * @param challengeId challenge identifier
     * @param page        page index
     * @param size        page size
     * @return paginated response
     */
    @GetMapping("/challenges/{challengeId}/submissions")
    public PageResponse<SubmissionResponse> listSubmissions(@PathVariable UUID challengeId,
                                                            @RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "20") int size) {
        return submissionService.listSubmissions(challengeId, page, size);
    }
}
