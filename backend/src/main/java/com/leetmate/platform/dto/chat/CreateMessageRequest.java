package com.leetmate.platform.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public class CreateMessageRequest {

    @NotBlank
    @Size(max = 8000)
    private String content;

    @Size(max = 30)
    private String codeLanguage;

    private UUID parentMessageId;

    public String getContent() {
        return content;
    }

    public String getCodeLanguage() {
        return codeLanguage;
    }

    public UUID getParentMessageId() {
        return parentMessageId;
    }
}
