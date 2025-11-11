package com.leetmate.platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Strongly typed configuration properties for interacting with the OpenAI API.
 */
@ConfigurationProperties(prefix = "chatgpt")
public class OpenAiProperties {

    private String baseUrl = "https://api.openai.com/v1";
    private String model = "gpt-4o-mini";
    private String apiKey;

    /**
     * @return the OpenAI REST base URL
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Sets the OpenAI REST base URL.
     *
     * @param baseUrl base URL to use
     */
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * @return the GPT model name
     */
    public String getModel() {
        return model;
    }

    /**
     * Sets the GPT model name.
     *
     * @param model desired model
     */
    public void setModel(String model) {
        this.model = model;
    }

    /**
     * @return the API key sourced from environment variables
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * Sets the API key used to authenticate against OpenAI.
     *
     * @param apiKey OpenAI API key
     */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
