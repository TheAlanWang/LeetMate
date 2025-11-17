package com.leetmate.platform.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "thread_id")
    private ChatThread thread;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private ChatMessage parent;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(length = 30)
    private String codeLanguage;

    @Column(nullable = false)
    private Instant createdAt;

    protected ChatMessage() {
    }

    public ChatMessage(UUID id, ChatThread thread, User author, String content, String codeLanguage, Instant createdAt) {
        this(id, thread, author, content, codeLanguage, createdAt, null);
    }

    public ChatMessage(UUID id,
                       ChatThread thread,
                       User author,
                       String content,
                       String codeLanguage,
                       Instant createdAt,
                       ChatMessage parent) {
        this.id = id;
        this.thread = thread;
        this.author = author;
        this.parent = parent;
        this.content = content;
        this.codeLanguage = codeLanguage;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public ChatThread getThread() {
        return thread;
    }

    public User getAuthor() {
        return author;
    }

    public ChatMessage getParent() {
        return parent;
    }

    public String getContent() {
        return content;
    }

    public String getCodeLanguage() {
        return codeLanguage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
