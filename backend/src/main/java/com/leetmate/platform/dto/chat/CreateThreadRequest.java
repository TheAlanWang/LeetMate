package com.leetmate.platform.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateThreadRequest {

    @NotBlank
    @Size(max = 160)
    private String title;

    @Size(max = 2000)
    private String description;

    @Size(max = 8000)
    private String initialMessage;

    @Size(max = 30)
    private String codeLanguage;

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getInitialMessage() {
        return initialMessage;
    }

    public String getCodeLanguage() {
        return codeLanguage;
    }
}
