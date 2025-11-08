package com.leetmate.platform.application;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST layer exposing mentee application workflows across browsing, submission, and mentor review actions.
 */
@Validated
@RestController
@RequestMapping
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping("/groups/{groupId}/apply")
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationResponse submitApplication(@PathVariable("groupId") Long groupId,
                                                 @Valid @RequestBody SubmitApplicationRequest request) {
        return applicationService.submitApplication(groupId, request);
    }

    @GetMapping("/groups/{groupId}/applications")
    public List<ApplicationResponse> listGroupApplications(@PathVariable("groupId") Long groupId,
                                                           @RequestParam("mentorId") Long mentorId) {
        return applicationService.listApplicationsForGroup(groupId, mentorId);
    }

    @PatchMapping("/applications/{applicationId}")
    public ApplicationResponse reviewApplication(@PathVariable("applicationId") Long applicationId,
                                                 @Valid @RequestBody UpdateApplicationStatusRequest request) {
        return applicationService.reviewApplication(applicationId, request);
    }

    @GetMapping("/users/{userId}/applications")
    public List<ApplicationResponse> listUserApplications(@PathVariable("userId") Long userId) {
        return applicationService.listApplicationsForUser(userId);
    }
}
