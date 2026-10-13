package com.sanket.AI.Code.Review.Platform.ai.exception;

public class RetrieverException extends RuntimeException {
    public RetrieverException(String message) {
        super(message);
    }

    public RetrieverException(String message, Throwable cause) {
        super(message, cause);
    }
}
