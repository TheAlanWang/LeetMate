package com.leetmate.platform.ai;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MockAiReviewProviderTest {

    @Test
    void mockProviderReturnsDeterministicSuggestions() {
        MockAiReviewProvider provider = new MockAiReviewProvider();
        AiReviewResult result = provider.review("java", "class Solution {}");

        assertThat(result.getSummary()).contains("java");
        assertThat(result.getSuggestions()).contains("Add more tests");
        assertThat(result.getCreatedAt()).isNotNull();
    }
}
