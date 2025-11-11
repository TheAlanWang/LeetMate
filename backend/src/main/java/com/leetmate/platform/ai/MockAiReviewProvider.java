package com.leetmate.platform.ai;

import java.time.Instant;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Deterministic AI provider used in tests to avoid external calls.
 */
@Component
@Profile("test")
public class MockAiReviewProvider implements AiReviewProvider {

    @Override
    public AiReviewResult review(String language, String code) {
        String summary = "Mock review for %s code (%d chars)".formatted(language, code.length());
        return new AiReviewResult(summary,
                List.of("Use descriptive variable names", "Add more tests"),
                Instant.now());
    }
}
