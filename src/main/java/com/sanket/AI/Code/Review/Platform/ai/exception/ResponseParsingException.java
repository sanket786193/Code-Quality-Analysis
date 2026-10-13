package com.sanket.AI.Code.Review.Platform.ai.exception;

public class ResponseParsingException extends RuntimeException {
    public ResponseParsingException(String message) {
        super(message);
    }

    public ResponseParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
