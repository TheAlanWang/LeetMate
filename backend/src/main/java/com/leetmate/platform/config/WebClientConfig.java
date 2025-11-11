package com.leetmate.platform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Provides common HTTP client beans.
 */
@Configuration
public class WebClientConfig {

    /**
     * Creates a shared {@link WebClient} instance with increased buffer sizes to handle AI responses.
     *
     * @return configured {@link WebClient}
     */
    @Bean
    public WebClient webClient() {
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(256 * 1024))
                .build();
        return WebClient.builder()
                .exchangeStrategies(strategies)
                .build();
    }
}
