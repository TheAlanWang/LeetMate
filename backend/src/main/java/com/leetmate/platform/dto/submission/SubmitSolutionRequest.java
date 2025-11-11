package com.leetmate.platform.dto.submission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for submitting a solution to a challenge.
 */
public class SubmitSolutionRequest {

    @NotBlank(message = "language must not be blank")
    private String language;

    @NotBlank(message = "code must not be blank")
    @Size(max = 10000, message = "code cannot exceed 10k characters")
    private String code;

    /**
     * @return language
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Sets the language.
     *
     * @param language programming language
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * @return code contents
     */
    public String getCode() {
        return code;
    }

    /**
     * Sets the code contents.
     *
     * @param code solution text
     */
    public void setCode(String code) {
        this.code = code;
    }
}
