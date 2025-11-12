package com.leetmate.platform.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate representing a mentor-led study group.
 */
@Entity
@Table(name = "study_groups")
public class StudyGroup {

    @Id
    private UUID id;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(nullable = false, length = 400)
    private String description;

    @ManyToOne(optional = false)
    @JoinColumn(name = "mentor_id")
    private User mentor;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "study_group_tags", joinColumns = @JoinColumn(name = "group_id"))
    @Column(name = "tag", length = 30)
    private List<String> tags = new ArrayList<>();

    @Column(nullable = false)
    private int memberCount = 0;

    @Column(nullable = false)
    private Instant createdAt;

    /**
     * JPA constructor.
     */
    protected StudyGroup() {
    }

    /**
     * Creates a new group instance.
     *
     * @param id          stable identifier
     * @param name        group name
     * @param description group description
     * @param tags        descriptive tags
     * @param createdAt   creation timestamp
     */
    public StudyGroup(UUID id,
                      User mentor,
                      String name,
                      String description,
                      List<String> tags,
                      Instant createdAt) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.mentor = Objects.requireNonNull(mentor, "mentor must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.name = name;
        this.description = description;
        this.tags = tags == null ? new ArrayList<>() : new ArrayList<>(tags);
        this.memberCount = 0;
    }

    /**
     * @return stable identifier
     */
    public UUID getId() {
        return id;
    }

    /**
     * @return current name
     */
    public String getName() {
        return name;
    }

    /**
     * Updates the group name.
     *
     * @param name new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return textual description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return mentor
     */
    public User getMentor() {
        return mentor;
    }

    /**
     * Updates the description.
     *
     * @param description new description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return tag list copy
     */
    public List<String> getTags() {
        return new ArrayList<>(tags);
    }

    /**
     * Replaces the current tag list.
     *
     * @param tags new tags
     */
    public void setTags(List<String> tags) {
        this.tags = tags == null ? new ArrayList<>() : new ArrayList<>(tags);
    }

    /**
     * @return current member count
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
     * Increments the member count.
     */
    public void incrementMembers() {
        this.memberCount++;
    }

    /**
     * Decrements the member count when possible.
     */
    public void decrementMembers() {
        if (memberCount == 0) {
            throw new IllegalStateException("Member count cannot be negative");
        }
        this.memberCount--;
    }
}
