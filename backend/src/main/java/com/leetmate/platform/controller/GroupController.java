package com.leetmate.platform.controller;

import com.leetmate.platform.dto.common.PageResponse;
import com.leetmate.platform.dto.group.CreateGroupRequest;
import com.leetmate.platform.dto.group.GroupResponse;
import com.leetmate.platform.security.UserPrincipal;
import com.leetmate.platform.service.GroupService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints for study group operations.
 */
@RestController
@RequestMapping("/groups")
@Validated
public class GroupController {

    private final GroupService groupService;

    /**
     * Creates a new controller.
     *
     * @param groupService service layer
     */
    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    /**
     * Creates a new study group.
     *
     * @param request payload
     * @return created group
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('MENTOR')")
    public GroupResponse createGroup(@AuthenticationPrincipal UserPrincipal user,
                                     @Valid @RequestBody CreateGroupRequest request) {
        return groupService.createGroup(request, user.getId());
    }

    /**
     * Lists groups using pagination.
     *
     * @param page zero-based page
     * @param size page size
     * @return paginated response
     */
    @GetMapping
    public PageResponse<GroupResponse> listGroups(@RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "20") int size) {
        return groupService.listGroups(page, size);
    }

    /**
     * Fetches group details.
     *
     * @param groupId identifier
     * @return group response
     */
    @GetMapping("/{groupId}")
    public GroupResponse getGroup(@PathVariable UUID groupId) {
        return groupService.getGroup(groupId);
    }

    /**
     * Adds a mentee to the group and increments the member count.
     *
     * @param groupId identifier
     * @return updated response
     */
    @PostMapping("/{groupId}/join")
    @PreAuthorize("hasRole('MENTEE')")
    public GroupResponse joinGroup(@AuthenticationPrincipal UserPrincipal user,
                                   @PathVariable UUID groupId) {
        return groupService.joinGroup(groupId, user.getId());
    }

    /**
     * Removes a mentee from the group.
     *
     * @param groupId identifier
     * @return updated response
     */
    @PostMapping("/{groupId}/leave")
    @PreAuthorize("hasRole('MENTEE')")
    public GroupResponse leaveGroup(@AuthenticationPrincipal UserPrincipal user,
                                    @PathVariable UUID groupId) {
        return groupService.leaveGroup(groupId, user.getId());
    }
}
