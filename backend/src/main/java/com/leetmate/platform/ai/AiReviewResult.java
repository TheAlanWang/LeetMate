package com.leetmate.platform.ai;

import java.time.Instant;
import java.util.List;

/**
 * Response from the AI review provider.
 */
public class AiReviewResult {

    private final String summary;
    private final List<String> suggestions;
    private final Instant createdAt;

    /**
     * Creates a new immutable result.
     *
     * @param summary     review summary
     * @param suggestions actionable suggestions
     * @param createdAt   review timestamp
     */
    public AiReviewResult(String summary, List<String> suggestions, Instant createdAt) {
        this.summary = summary;
        this.suggestions = List.copyOf(suggestions);
        this.createdAt = createdAt;
    }

    /**
     * @return review summary
     */
    public String getSummary() {
        return summary;
    }

    /**
     * @return suggestions
     */
    public List<String> getSuggestions() {
        return suggestions;
    }

    /**
     * @return timestamp
     */
    public Instant getCreatedAt() {
        return createdAt;
    }
}
