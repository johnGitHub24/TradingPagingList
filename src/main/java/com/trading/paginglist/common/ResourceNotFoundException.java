package com.trading.paginglist.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown by the service layer when a requested resource does not exist in the database.
 *
 * <p>The {@link ResponseStatus} annotation causes Spring MVC to return HTTP 404
 * automatically when this exception propagates out of a controller method.
 * {@link GlobalExceptionHandler} provides a more structured JSON body.</p>
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructs a new exception with a descriptive message that identifies
     * the missing resource.
     *
     * @param message human-readable description of the missing resource
     *                (e.g. "Product not found with id: 42")
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
