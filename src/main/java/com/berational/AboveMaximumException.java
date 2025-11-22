package com.berational;

/**
 * Exception thrown when option price is above maximum possible value.
 *
 * For calls: maximum value is the forward price F
 * For puts: maximum value is the strike price K
 */
public class AboveMaximumException extends RuntimeException {

    public AboveMaximumException() {
        super("Option price is above maximum possible value");
    }

    public AboveMaximumException(String message) {
        super(message);
    }

    public AboveMaximumException(String message, Throwable cause) {
        super(message, cause);
    }
}
