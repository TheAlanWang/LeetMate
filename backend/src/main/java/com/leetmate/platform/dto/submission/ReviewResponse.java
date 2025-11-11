package com.leetmate.platform.dto.submission;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DTO describing the AI review returned alongside submissions.
 */
public class ReviewResponse {

    private final UUID id;
    private final String summary;
    private final int complexity;
    private final List<String> suggestions;
    private final Instant createdAt;

    /**
     * Creates a new immutable response.
     *
     * @param id          identifier
     * @param summary     summary text
     * @param complexity  cyclomatic complexity
     * @param suggestions improvement ideas
     * @param createdAt   timestamp
     */
    public ReviewResponse(UUID id,
                          String summary,
                          int complexity,
                          List<String> suggestions,
                          Instant createdAt) {
        this.id = id;
        this.summary = summary;
        this.complexity = complexity;
        this.suggestions = List.copyOf(suggestions);
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getSummary() {
        return summary;
    }

    public int getComplexity() {
        return complexity;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
