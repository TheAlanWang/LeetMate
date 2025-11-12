package com.leetmate.platform.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Membership relation between mentees and study groups.
 */
@Entity
@Table(name = "group_members", uniqueConstraints = @UniqueConstraint(columnNames = {"group_id", "member_id"}))
public class GroupMember {

    @Id
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private StudyGroup group;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private User member;

    @Column(nullable = false)
    private Instant joinedAt;

    protected GroupMember() {
    }

    public GroupMember(UUID id, StudyGroup group, User member, Instant joinedAt) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.group = Objects.requireNonNull(group, "group must not be null");
        this.member = Objects.requireNonNull(member, "member must not be null");
        this.joinedAt = Objects.requireNonNull(joinedAt, "joinedAt must not be null");
    }

    public UUID getId() {
        return id;
    }

    public StudyGroup getGroup() {
        return group;
    }

    public User getMember() {
        return member;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }
}
