package com.leetmate.platform.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * AI review embedded within a submission.
 */
@Entity
@Table(name = "submission_reviews")
public class SubmissionReview {

    @Id
    private UUID id;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String summary;

    @Column(nullable = false)
    private int complexity;

    @ElementCollection
    @CollectionTable(name = "submission_review_suggestions", joinColumns = @JoinColumn(name = "review_id"))
    @Column(name = "suggestion", columnDefinition = "TEXT")
    private List<String> suggestions = new ArrayList<>();

    protected SubmissionReview() {
    }

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
