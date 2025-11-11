package com.leetmate.platform;

import com.leetmate.platform.config.OpenAiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Entry point for the LeetMate backend application.
 */
@SpringBootApplication
@EnableConfigurationProperties(OpenAiProperties.class)
public class LeetMateApplication {

    /**
     * Boots the Spring context.
     *
     * @param args application arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(LeetMateApplication.class, args);
    }
}
