package com.leetmate.platform.entity;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a coding challenge posted inside a study group.
 */
public class Challenge {

    private final UUID id;
    private final UUID groupId;
    private final Instant createdAt;
    private String title;
    private String description;
    private String language;
    private ChallengeDifficulty difficulty;
    private String starterCode;

    /**
     * Creates a new challenge instance.
     *
     * @param id          identifier
     * @param groupId     parent group identifier
     * @param title       title
     * @param description description
     * @param language    preferred language
     * @param difficulty  difficulty level
     * @param starterCode starter template
     * @param createdAt   creation timestamp
     */
    public Challenge(UUID id,
                     UUID groupId,
                     String title,
                     String description,
                     String language,
                     ChallengeDifficulty difficulty,
                     String starterCode,
                     Instant createdAt) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.groupId = Objects.requireNonNull(groupId, "groupId must not be null");
        this.title = title;
        this.description = description;
        this.language = language;
        this.difficulty = difficulty;
        this.starterCode = starterCode;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
    }

    /**
     * @return identifier
     */
    public UUID getId() {
        return id;
    }

    /**
     * @return owning group identifier
     */
    public UUID getGroupId() {
        return groupId;
    }

    /**
     * @return challenge title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return challenge description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return preferred language
     */
    public String getLanguage() {
        return language;
    }

    /**
     * @return difficulty
     */
    public ChallengeDifficulty getDifficulty() {
        return difficulty;
    }

    /**
     * @return starter code snippet
     */
    public String getStarterCode() {
        return starterCode;
    }

    /**
     * @return creation timestamp
     */
    public Instant getCreatedAt() {
        return createdAt;
    }
}
