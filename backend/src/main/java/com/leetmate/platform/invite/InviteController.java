package com.leetmate.platform.invite;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
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
 * API surface for mentor-issued invitations and mentee responses per the PRD invite workflow.
 */
@Validated
@RestController
@RequestMapping
public class InviteController {

    private final InviteService inviteService;

    public InviteController(InviteService inviteService) {
        this.inviteService = inviteService;
    }

    @PostMapping("/groups/{groupId}/invite")
    @ResponseStatus(HttpStatus.CREATED)
    public InviteResponse sendInvite(@PathVariable("groupId") Long groupId,
                                     @Valid @RequestBody SendInviteRequest request) {
        return inviteService.sendInvite(groupId, request);
    }

    @GetMapping("/users/{userId}/invites")
    public List<InviteResponse> listInvites(@PathVariable("userId") Long userId) {
        return inviteService.listInvitesForUser(userId);
    }

    @PatchMapping("/invites/{inviteId}")
    public InviteResponse respondToInvite(@PathVariable("inviteId") Long inviteId,
                                          @Valid @RequestBody InviteDecisionRequest request) {
        return inviteService.respondToInvite(inviteId, request);
    }

    @DeleteMapping("/invites/{inviteId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelInvite(@PathVariable("inviteId") Long inviteId,
                             @RequestParam("mentorId") Long mentorId) {
        inviteService.deleteInvite(inviteId, mentorId);
    }
}
