package com.leetmate.platform.ai;

/**
 * Contract for supplying ChatGPT style reviews for submissions.
 */
public interface AiReviewProvider {

    /**
     * Performs an AI review of the provided code.
     *
     * @param language submission language
     * @param code     submission code
     * @return AI review result
     */
    AiReviewResult review(String language, String code);
}
