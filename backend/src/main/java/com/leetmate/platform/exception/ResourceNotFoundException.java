package com.leetmate.platform.exception;

/**
 * Signals missing domain objects.
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Creates a new exception with the provided message.
     *
     * @param message human readable context
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
