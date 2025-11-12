package com.leetmate.platform.dto.group;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Representation of a study group returned to API consumers.
 */
public class GroupResponse {

    private final UUID id;
    private final String name;
    private final String description;
    private final List<String> tags;
    private final int memberCount;
    private final Instant createdAt;
    private final UUID mentorId;
    private final String mentorName;

    /**
     * Creates a new response.
     *
     * @param id          identifier
     * @param name        name
     * @param description description
     * @param tags        tags
     * @param memberCount members
     * @param createdAt   timestamp
     * @param mentorId    mentor identifier
     * @param mentorName  mentor name
     */
    public GroupResponse(UUID id,
                         String name,
                         String description,
                         List<String> tags,
                         int memberCount,
                         Instant createdAt,
                         UUID mentorId,
                         String mentorName) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.tags = List.copyOf(tags);
        this.memberCount = memberCount;
        this.createdAt = createdAt;
        this.mentorId = mentorId;
        this.mentorName = mentorName;
    }

    /**
     * @return identifier
     */
    public UUID getId() {
        return id;
    }

    /**
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return tags
     */
    public List<String> getTags() {
        return tags;
    }

    /**
     * @return member count
     */
    public int getMemberCount() {
        return memberCount;
    }

    /**
     * @return creation timestamp
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * @return mentor identifier
     */
    public UUID getMentorId() {
        return mentorId;
    }

    /**
     * @return mentor display name
     */
    public String getMentorName() {
        return mentorName;
    }
}
