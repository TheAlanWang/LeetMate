package com.leetmate.platform.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;  // recipient

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private StudyGroup group;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "thread_id")
    private ChatThread thread;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id")
    private ChatMessage message;  // only for THREAD_REPLY

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private User actor;  // who triggered the notification

    @Column(nullable = false)
    private boolean read = false;

    @Column(nullable = false)
    private Instant createdAt;

    protected Notification() {
    }

    public Notification(UUID id,
                       User user,
                       NotificationType type,
                       StudyGroup group,
                       ChatThread thread,
                       ChatMessage message,
                       User actor,
                       Instant createdAt) {
        this.id = id;
        this.user = user;
        this.type = type;
        this.group = group;
        this.thread = thread;
        this.message = message;
        this.actor = actor;
        this.read = false;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public NotificationType getType() {
        return type;
    }

    public StudyGroup getGroup() {
        return group;
    }

    public ChatThread getThread() {
        return thread;
    }

    public ChatMessage getMessage() {
        return message;
    }

    public User getActor() {
        return actor;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

