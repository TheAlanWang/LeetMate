package com.leetmate.platform.dto.chat;

import java.time.Instant;
import java.util.UUID;

public class MessageResponse {

    private final UUID id;
    private final UUID threadId;
    private final UUID authorId;
    private final String authorName;
    private final String authorRole;
    private final String content;
    private final String codeLanguage;
    private final Instant createdAt;
    private final UUID parentMessageId;

    public MessageResponse(UUID id,
                           UUID threadId,
                           UUID authorId,
                           String authorName,
                           String authorRole,
                           String content,
                           String codeLanguage,
                           Instant createdAt,
                           UUID parentMessageId) {
        this.id = id;
        this.threadId = threadId;
        this.authorId = authorId;
        this.authorName = authorName;
        this.authorRole = authorRole;
        this.content = content;
        this.codeLanguage = codeLanguage;
        this.createdAt = createdAt;
        this.parentMessageId = parentMessageId;
    }

    public UUID getId() {
        return id;
    }

    public UUID getThreadId() {
        return threadId;
    }

    public UUID getAuthorId() {
        return authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getAuthorRole() {
        return authorRole;
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

    public UUID getParentMessageId() {
        return parentMessageId;
    }
}
