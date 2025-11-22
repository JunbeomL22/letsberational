package com.berational;

/**
 * Exception thrown when option price is below intrinsic value.
 *
 * This indicates an arbitrage opportunity or invalid input data,
 * as an option should never trade below its intrinsic value.
 */
public class BelowIntrinsicException extends RuntimeException {

    public BelowIntrinsicException() {
        super("Option price is below intrinsic value");
    }

    public BelowIntrinsicException(String message) {
        super(message);
    }

    public BelowIntrinsicException(String message, Throwable cause) {
        super(message, cause);
    }
}
