package com.leetmate.platform.dto.group;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Request payload for creating study groups.
 */
public class CreateGroupRequest {

    @NotBlank(message = "name must not be blank")
    @Size(max = 80, message = "name can have at most 80 characters")
    private String name;

    @NotBlank(message = "description must not be blank")
    @Size(max = 400, message = "description can have at most 400 characters")
    private String description;

    @NotEmpty(message = "tags must not be empty")
    @Size(max = 5, message = "tags can have at most 5 entries")
    private List<@Size(max = 30, message = "tag must be at most 30 characters") String> tags;

    /**
     * @return desired name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the group name.
     *
     * @param name group name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return textual description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description.
     *
     * @param description group description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return list of tags
     */
    public List<String> getTags() {
        return tags;
    }

    /**
     * Sets the tags.
     *
     * @param tags tag list
     */
    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
