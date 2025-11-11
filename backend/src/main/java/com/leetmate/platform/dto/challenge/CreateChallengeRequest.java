package com.leetmate.platform.dto.challenge;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request payload for creating a challenge within a group.
 */
public class CreateChallengeRequest {

    @NotBlank(message = "title must not be blank")
    @Size(max = 120, message = "title can have at most 120 characters")
    private String title;

    @NotBlank(message = "description must not be blank")
    private String description;

    @NotBlank(message = "language must not be blank")
    @Pattern(regexp = "(?i)java|python|cpp|js", message = "language must be one of java, python, cpp, js")
    private String language;

    @NotBlank(message = "difficulty must not be blank")
    @Pattern(regexp = "(?i)easy|medium|hard", message = "difficulty must be EASY, MEDIUM or HARD")
    private String difficulty;

    @NotBlank(message = "starterCode must not be blank")
    private String starterCode;

    /**
     * @return title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title.
     *
     * @param title new title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description.
     *
     * @param description new description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return language
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Sets the preferred language.
     *
     * @param language programming language
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * @return difficulty string
     */
    public String getDifficulty() {
        return difficulty;
    }

    /**
     * Sets the difficulty.
     *
     * @param difficulty difficulty string
     */
    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    /**
     * @return starter code snippet
     */
    public String getStarterCode() {
        return starterCode;
    }

    /**
     * Sets the starter code snippet.
     *
     * @param starterCode snippet
     */
    public void setStarterCode(String starterCode) {
        this.starterCode = starterCode;
    }
}
