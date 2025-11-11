package com.leetmate.platform.entity;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Represents a mentee submission for a challenge.
 */
public class Submission {

    private final UUID id;
    private final UUID challengeId;
    private final Instant createdAt;
    private final String language;
    private final String code;
    private final int creditsAwarded;
    private SubmissionReview review;

    /**
     * Creates a new submission.
     *
     * @param id             identifier
     * @param challengeId    owning challenge identifier
     * @param language       language used by the mentee
     * @param code           code content
     * @param creditsAwarded credits rewarded for the submission
     * @param createdAt      creation timestamp
     */
    public Submission(UUID id,
                      UUID challengeId,
                      String language,
                      String code,
                      int creditsAwarded,
                      Instant createdAt) {
        this.id = id;
        this.challengeId = challengeId;
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
        return challengeId;
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
