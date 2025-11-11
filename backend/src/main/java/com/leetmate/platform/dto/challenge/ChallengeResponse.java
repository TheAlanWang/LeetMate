package com.leetmate.platform.dto.challenge;

import com.leetmate.platform.entity.ChallengeDifficulty;
import java.time.Instant;
import java.util.UUID;

/**
 * REST representation of a challenge.
 */
public class ChallengeResponse {

    private final UUID id;
    private final UUID groupId;
    private final String title;
    private final String description;
    private final String language;
    private final ChallengeDifficulty difficulty;
    private final String starterCode;
    private final Instant createdAt;

    /**
     * Creates a new response.
     *
     * @param id          identifier
     * @param groupId     owning group
     * @param title       title
     * @param description description
     * @param language    language
     * @param difficulty  difficulty
     * @param starterCode starter snippet
     * @param createdAt   timestamp
     */
    public ChallengeResponse(UUID id,
                             UUID groupId,
                             String title,
                             String description,
                             String language,
                             ChallengeDifficulty difficulty,
                             String starterCode,
                             Instant createdAt) {
        this.id = id;
        this.groupId = groupId;
        this.title = title;
        this.description = description;
        this.language = language;
        this.difficulty = difficulty;
        this.starterCode = starterCode;
        this.createdAt = createdAt;
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

    public String getLanguage() {
        return language;
    }

    public ChallengeDifficulty getDifficulty() {
        return difficulty;
    }

    public String getStarterCode() {
        return starterCode;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
