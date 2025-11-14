package com.leetmate.platform.controller;

import com.leetmate.platform.dto.chat.CreateMessageRequest;
import com.leetmate.platform.dto.chat.CreateThreadRequest;
import com.leetmate.platform.dto.chat.MessageResponse;
import com.leetmate.platform.dto.chat.ThreadResponse;
import com.leetmate.platform.dto.common.PageResponse;
import com.leetmate.platform.security.UserPrincipal;
import com.leetmate.platform.service.GroupChatService;
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

@RestController
@RequestMapping
@Validated
public class GroupChatController {

    private final GroupChatService groupChatService;

    public GroupChatController(GroupChatService groupChatService) {
        this.groupChatService = groupChatService;
    }

    @PostMapping("/groups/{groupId}/threads")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('MENTOR','MENTEE')")
    public ThreadResponse createThread(@PathVariable UUID groupId,
                                       @AuthenticationPrincipal UserPrincipal user,
                                       @Valid @RequestBody CreateThreadRequest request) {
        return groupChatService.createThread(groupId, user.getId(), request);
    }

    @GetMapping("/groups/{groupId}/threads")
    @PreAuthorize("hasAnyRole('MENTOR','MENTEE')")
    public PageResponse<ThreadResponse> listThreads(@PathVariable UUID groupId,
                                                    @AuthenticationPrincipal UserPrincipal user,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "20") int size) {
        return groupChatService.listThreads(groupId, user.getId(), page, size);
    }

    @PostMapping("/threads/{threadId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('MENTOR','MENTEE')")
    public MessageResponse postMessage(@PathVariable UUID threadId,
                                       @AuthenticationPrincipal UserPrincipal user,
                                       @Valid @RequestBody CreateMessageRequest request) {
        return groupChatService.postMessage(threadId, user.getId(), request);
    }

    @GetMapping("/threads/{threadId}/messages")
    @PreAuthorize("hasAnyRole('MENTOR','MENTEE')")
    public PageResponse<MessageResponse> listMessages(@PathVariable UUID threadId,
                                                      @AuthenticationPrincipal UserPrincipal user,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "50") int size) {
        return groupChatService.listMessages(threadId, user.getId(), page, size);
    }
}
