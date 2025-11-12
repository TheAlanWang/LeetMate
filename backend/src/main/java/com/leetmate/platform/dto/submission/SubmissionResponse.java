package com.leetmate.platform.dto.submission;

import java.time.Instant;
import java.util.UUID;

/**
 * Submission response structure.
 */
public class SubmissionResponse {

    private final UUID id;
    private final UUID challengeId;
    private final UUID menteeId;
    private final String menteeName;
    private final String language;
    private final String code;
    private final int creditsAwarded;
    private final Instant createdAt;
    private final ReviewResponse review;

    /**
     * Creates a new response.
     *
     * @param id             identifier
     * @param challengeId    challenge identifier
     * @param language       language
     * @param code           code
     * @param creditsAwarded credits
     * @param createdAt      timestamp
     * @param review         AI review
     */
    public SubmissionResponse(UUID id,
                              UUID challengeId,
                              UUID menteeId,
                              String menteeName,
                              String language,
                              String code,
                              int creditsAwarded,
                              Instant createdAt,
                              ReviewResponse review) {
        this.id = id;
        this.challengeId = challengeId;
        this.menteeId = menteeId;
        this.menteeName = menteeName;
        this.language = language;
        this.code = code;
        this.creditsAwarded = creditsAwarded;
        this.createdAt = createdAt;
        this.review = review;
    }

    public UUID getId() {
        return id;
    }

    public UUID getChallengeId() {
        return challengeId;
    }

    public UUID getMenteeId() {
        return menteeId;
    }

    public String getMenteeName() {
        return menteeName;
    }

    public String getLanguage() {
        return language;
    }

    public String getCode() {
        return code;
    }

    public int getCreditsAwarded() {
        return creditsAwarded;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public ReviewResponse getReview() {
        return review;
    }
}
