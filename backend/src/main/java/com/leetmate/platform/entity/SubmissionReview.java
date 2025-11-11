package com.leetmate.platform.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * AI review embedded within a submission.
 */
public class SubmissionReview {

    private final UUID id;
    private final Instant createdAt;
    private final String summary;
    private final int complexity;
    private final List<String> suggestions;

    /**
     * Creates a new review snapshot.
     *
     * @param id          review identifier
     * @param createdAt   creation timestamp
     * @param summary     textual summary
     * @param complexity  cyclomatic complexity
     * @param suggestions AI suggestions
     */
    public SubmissionReview(UUID id,
                            Instant createdAt,
                            String summary,
                            int complexity,
                            List<String> suggestions) {
        this.id = id;
        this.createdAt = createdAt;
        this.summary = summary;
        this.complexity = complexity;
        this.suggestions = new ArrayList<>(suggestions);
    }

    /**
     * @return identifier
     */
    public UUID getId() {
        return id;
    }

    /**
     * @return timestamp
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * @return AI summary
     */
    public String getSummary() {
        return summary;
    }

    /**
     * @return computed complexity
     */
    public int getComplexity() {
        return complexity;
    }

    /**
     * @return suggestions copy
     */
    public List<String> getSuggestions() {
        return new ArrayList<>(suggestions);
    }
}
