package com.sanket.AI.Code.Review.Platform.exception;

public class ReviewJobCreationException extends RuntimeException {
    public ReviewJobCreationException(String message) {
        super(message);
    }

    public ReviewJobCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
