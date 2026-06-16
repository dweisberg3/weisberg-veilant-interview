package com.veilant.datatable.registry;

/**
 * Thrown when a submitted API URL can't be registered: unreachable,
 * not JSON, not an array, empty, or containing nested objects/arrays
 * (i.e. not "flat"). The message is sent straight back to the client
 * as the HTTP 400 error body.
 */
public class SchemaValidationException extends Exception {
    public SchemaValidationException(String message) {
        super(message);
    }
}
