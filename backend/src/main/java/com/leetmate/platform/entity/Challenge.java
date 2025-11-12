package com.leetmate.platform.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a coding challenge posted inside a study group.
 */
@Entity
@Table(name = "challenges")
public class Challenge {

    @Id
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "group_id")
    private StudyGroup group;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 20)
    private String language;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ChallengeDifficulty difficulty;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String starterCode;

    @Column(nullable = false)
    private Instant createdAt;

    protected Challenge() {
    }

    /**
     * Creates a new challenge instance.
     *
     * @param id          identifier
     * @param group       parent group
     * @param title       title
     * @param description description
     * @param language    preferred language
     * @param difficulty  difficulty level
     * @param starterCode starter template
     * @param createdAt   creation timestamp
     */
    public Challenge(UUID id,
                     StudyGroup group,
                     String title,
                     String description,
                     String language,
                     ChallengeDifficulty difficulty,
                     String starterCode,
                     Instant createdAt) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.group = Objects.requireNonNull(group, "group must not be null");
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
        return group.getId();
    }

    public StudyGroup getGroup() {
        return group;
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
