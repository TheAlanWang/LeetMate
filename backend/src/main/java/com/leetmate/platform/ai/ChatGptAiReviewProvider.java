package com.leetmate.platform.ai;

import com.leetmate.platform.config.OpenAiProperties;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Production-ready OpenAI backed implementation of {@link AiReviewProvider}.
 */
@Component
@Profile("!test")
public class ChatGptAiReviewProvider implements AiReviewProvider {

    private static final String PROMPT_TEMPLATE = """
            You are a senior software engineer.
            Review the following %s code. Provide:
            1. A concise summary (<=3 sentences)
            2. Potential issues or errors
            3. Suggestions for improvement
            4. Optional performance or complexity notes
            
            Respond in plain text with bullet points for suggestions.
            
            <CODE>
            %s
            </CODE>
            """;

    private final WebClient webClient;
    private final OpenAiProperties properties;

    /**
     * Creates a new provider.
     *
     * @param webClient  shared WebClient
     * @param properties OpenAI configuration
     */
    public ChatGptAiReviewProvider(WebClient webClient, OpenAiProperties properties) {
        this.webClient = webClient;
        this.properties = properties;
    }

    @Override
    public AiReviewResult review(String language, String code) {
        if (!StringUtils.hasText(properties.getApiKey())) {
            throw new IllegalStateException("OpenAI API key is not configured");
        }
        String payload = """
                {
                  "model": "%s",
                  "messages": [
                    {"role": "system", "content": "You are a helpful AI code reviewer."},
                    {"role": "user", "content": %s}
                  ],
                  "temperature": 0.2
                }
                """.formatted(
                properties.getModel(),
                toJsonString(PROMPT_TEMPLATE.formatted(language, code))
        );

        ChatCompletionResponse response = webClient.post()
                .uri(properties.getBaseUrl() + "/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + properties.getApiKey())
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(ChatCompletionResponse.class)
                .block();

        String content = response == null ? "" : response.messageContent();
        if (!StringUtils.hasText(content)) {
            content = "No review generated.";
        }

        String[] lines = content.split("\\r?\\n");
        String summary = lines.length > 0 ? lines[0].trim() : content.trim();
        List<String> suggestions = Arrays.stream(lines)
                .map(String::trim)
                .filter(line -> line.startsWith("-") || line.startsWith("*"))
                .map(line -> line.replaceFirst("^[*-]\\s*", ""))
                .collect(Collectors.toList());
        return new AiReviewResult(summary, suggestions, Instant.now());
    }

    private String toJsonString(String input) {
        String escaped = input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
        return "\"" + escaped + "\"";
    }

    private record ChatCompletionResponse(List<Choice> choices) {
        private String messageContent() {
            if (choices == null || choices.isEmpty()) {
                return "";
            }
            Message message = choices.get(0).message();
            return message == null || message.content() == null ? "" : message.content();
        }
    }

    private record Choice(Message message) {
    }

    private record Message(String role, String content) {
    }
}
