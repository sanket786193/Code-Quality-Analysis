package com.sanket.AI.Code.Review.Platform.exception;

public class WebhookValidationException extends RuntimeException {
    public WebhookValidationException(String message) {
        super(message);
    }
}
