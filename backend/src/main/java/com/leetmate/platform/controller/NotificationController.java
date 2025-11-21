package com.leetmate.platform.controller;

import com.leetmate.platform.dto.common.PageResponse;
import com.leetmate.platform.dto.notification.NotificationCountResponse;
import com.leetmate.platform.dto.notification.NotificationResponse;
import com.leetmate.platform.security.UserPrincipal;
import com.leetmate.platform.service.NotificationService;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints for notification operations.
 */
@RestController
@RequestMapping("/notifications")
@Validated
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Get paginated list of notifications for the authenticated user.
     *
     * @param user authenticated user
     * @param page zero-based page index
     * @param size page size
     * @param unreadOnly if true, only return unread notifications
     * @return paginated notifications
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('MENTOR','MENTEE')")
    public PageResponse<NotificationResponse> getNotifications(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Boolean unreadOnly) {
        return notificationService.getNotifications(
            user.getId(),
            page,
            size,
            unreadOnly != null && unreadOnly
        );
    }

    /**
     * Get unread notification count for the authenticated user.
     *
     * @param user authenticated user
     * @return unread count
     */
    @GetMapping("/count")
    @PreAuthorize("hasAnyRole('MENTOR','MENTEE')")
    public NotificationCountResponse getUnreadCount(@AuthenticationPrincipal UserPrincipal user) {
        return notificationService.getUnreadCount(user.getId());
    }

    /**
     * Mark a specific notification as read.
     *
     * @param user authenticated user
     * @param notificationId notification identifier
     */
    @PutMapping("/{notificationId}/read")
    @PreAuthorize("hasAnyRole('MENTOR','MENTEE')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAsRead(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID notificationId) {
        notificationService.markAsRead(notificationId, user.getId());
    }

    /**
     * Mark all notifications as read for the authenticated user.
     *
     * @param user authenticated user
     */
    @PutMapping("/read-all")
    @PreAuthorize("hasAnyRole('MENTOR','MENTEE')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAllAsRead(@AuthenticationPrincipal UserPrincipal user) {
        notificationService.markAllAsRead(user.getId());
    }
}


