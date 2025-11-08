package com.leetmate.platform.group;

import com.leetmate.platform.membership.MemberResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/groups")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GroupResponse createGroup(@Valid @RequestBody CreateGroupRequest request) {
        return groupService.createGroup(request);
    }

    @GetMapping
    public List<GroupResponse> listGroups() {
        return groupService.listGroups();
    }

    @GetMapping("/{id}")
    public GroupResponse getGroup(@PathVariable("id") Long id) {
        return groupService.getGroup(id);
    }

    @GetMapping("/{id}/members")
    public List<MemberResponse> getMembers(@PathVariable("id") Long id) {
        return groupService.getMembers(id);
    }
}
