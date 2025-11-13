package com.leetmate.platform.dto.chat;

import java.time.Instant;
import java.util.UUID;

public class ThreadResponse {

    private final UUID id;
    private final UUID groupId;
    private final String title;
    private final String description;
    private final Instant createdAt;
    private final UUID createdById;
    private final String createdByName;

    public ThreadResponse(UUID id,
                          UUID groupId,
                          String title,
                          String description,
                          Instant createdAt,
                          UUID createdById,
                          String createdByName) {
        this.id = id;
        this.groupId = groupId;
        this.title = title;
        this.description = description;
        this.createdAt = createdAt;
        this.createdById = createdById;
        this.createdByName = createdByName;
    }

    public UUID getId() {
        return id;
    }

    public UUID getGroupId() {
        return groupId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public UUID getCreatedById() {
        return createdById;
    }

    public String getCreatedByName() {
        return createdByName;
    }
}
