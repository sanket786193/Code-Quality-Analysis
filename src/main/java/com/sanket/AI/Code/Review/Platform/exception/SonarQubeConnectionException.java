package com.sanket.AI.Code.Review.Platform.exception;

public class SonarQubeConnectionException extends RuntimeException {
    public SonarQubeConnectionException(String message) {
        super(message);
    }

    public SonarQubeConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
