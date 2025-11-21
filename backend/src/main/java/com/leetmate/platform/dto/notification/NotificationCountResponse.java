package com.leetmate.platform.dto.notification;

/**
 * Response DTO for unread notification count.
 */
public class NotificationCountResponse {

    private final int unreadCount;

    public NotificationCountResponse(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public int getUnreadCount() {
        return unreadCount;
    }
}


