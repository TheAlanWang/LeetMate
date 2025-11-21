package com.leetmate.platform.dto.notification;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for a notification.
 */
public class NotificationResponse {

    private final UUID id;
    private final String type;
    private final boolean read;
    private final Instant createdAt;
    private final GroupInfo group;
    private final ThreadInfo thread;
    private final ActorInfo actor;
    private final MessagePreview message;

    public NotificationResponse(UUID id,
                               String type,
                               boolean read,
                               Instant createdAt,
                               GroupInfo group,
                               ThreadInfo thread,
                               ActorInfo actor,
                               MessagePreview message) {
        this.id = id;
        this.type = type;
        this.read = read;
        this.createdAt = createdAt;
        this.group = group;
        this.thread = thread;
        this.actor = actor;
        this.message = message;
    }

    public UUID getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public boolean isRead() {
        return read;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public GroupInfo getGroup() {
        return group;
    }

    public ThreadInfo getThread() {
        return thread;
    }

    public ActorInfo getActor() {
        return actor;
    }

    public MessagePreview getMessage() {
        return message;
    }

    /**
     * Group information for the notification.
     */
    public static class GroupInfo {
        private final UUID id;
        private final String name;

        public GroupInfo(UUID id, String name) {
            this.id = id;
            this.name = name;
        }

        public UUID getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    /**
     * Thread information for the notification.
     */
    public static class ThreadInfo {
        private final UUID id;
        private final String title;

        public ThreadInfo(UUID id, String title) {
            this.id = id;
            this.title = title;
        }

        public UUID getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }
    }

    /**
     * Actor information (who triggered the notification).
     */
    public static class ActorInfo {
        private final UUID id;
        private final String name;
        private final String role;

        public ActorInfo(UUID id, String name, String role) {
            this.id = id;
            this.name = name;
            this.role = role;
        }

        public UUID getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getRole() {
            return role;
        }
    }

    /**
     * Message preview (only for THREAD_REPLY notifications).
     */
    public static class MessagePreview {
        private final UUID id;
        private final String preview;

        public MessagePreview(UUID id, String preview) {
            this.id = id;
            this.preview = preview;
        }

        public UUID getId() {
            return id;
        }

        public String getPreview() {
            return preview;
        }
    }
}


