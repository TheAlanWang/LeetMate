package com.leetmate.platform.exception;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simple controller used by {@link GlobalExceptionHandlerTest} to trigger validation errors and not-found scenarios.
 */
@RestController
@RequestMapping("/test")
public class TestExceptionController {

    @PostMapping
    public String validate(@Valid @RequestBody DummyRequest request) {
        return "ok";
    }

    @GetMapping
    public String notFound() {
        throw new ResourceNotFoundException("missing");
    }

    public static class DummyRequest {
        @NotBlank
        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
