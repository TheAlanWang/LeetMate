package com.leetmate.platform.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Represents a mentee submission for a challenge.
 */
@Entity
@Table(name = "submissions")
public class Submission {

    @Id
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "challenge_id")
    private Challenge challenge;

    @ManyToOne(optional = false)
    @JoinColumn(name = "mentee_id")
    private User mentee;

    @Column(nullable = false, length = 20)
    private String language;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String code;

    @Column(nullable = false)
    private int creditsAwarded;

    @Column(nullable = false)
    private Instant createdAt;

    @OneToOne(optional = true, orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "review_id")
    private SubmissionReview review;

    protected Submission() {
    }

    /**
     * Creates a new submission.
     *
     * @param id             identifier
     * @param challenge      owning challenge
     * @param language       language used by the mentee
     * @param code           code content
     * @param creditsAwarded credits rewarded for the submission
     * @param createdAt      creation timestamp
     */
    public Submission(UUID id,
                      Challenge challenge,
                      User mentee,
                      String language,
                      String code,
                      int creditsAwarded,
                      Instant createdAt) {
        this.id = id;
        this.challenge = challenge;
        this.mentee = mentee;
        this.language = language;
        this.code = code;
        this.creditsAwarded = creditsAwarded;
        this.createdAt = createdAt;
    }

    /**
     * @return identifier
     */
    public UUID getId() {
        return id;
    }

    /**
     * @return challenge identifier
     */
    public UUID getChallengeId() {
        return challenge.getId();
    }

    public Challenge getChallenge() {
        return challenge;
    }

    public User getMentee() {
        return mentee;
    }

    /**
     * @return submission time
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * @return programming language
     */
    public String getLanguage() {
        return language;
    }

    /**
     * @return submitted code
     */
    public String getCode() {
        return code;
    }

    /**
     * @return credits earned
     */
    public int getCreditsAwarded() {
        return creditsAwarded;
    }

    /**
     * @return optional review
     */
    public Optional<SubmissionReview> getReview() {
        return Optional.ofNullable(review);
    }

    /**
     * Attaches the AI review to the submission.
     *
     * @param review review details
     */
    public void attachReview(SubmissionReview review) {
        this.review = review;
    }
}
