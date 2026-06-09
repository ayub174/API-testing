package com.romkalkylator.exception;

/**
 * Kastas när en uppladdad fil saknas, är tom eller inte kan tolkas som en
 * giltig Excel-fil.
 */
public class OgiltigFilException extends RuntimeException {

    public OgiltigFilException(String message) {
        super(message);
    }

    public OgiltigFilException(String message, Throwable cause) {
        super(message, cause);
    }
}
