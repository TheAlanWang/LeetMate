package com.leetmate.platform.dto.chat;

import jakarta.validation.constraints.NotBlank;

public class UpdateMessageRequest {

    @NotBlank(message = "Content is required")
    private String content;

    public UpdateMessageRequest() {
    }

    public UpdateMessageRequest(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
