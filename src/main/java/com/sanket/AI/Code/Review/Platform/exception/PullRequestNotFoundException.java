package com.sanket.AI.Code.Review.Platform.exception;

public class PullRequestNotFoundException extends RuntimeException {
    public PullRequestNotFoundException(String message) {
        super(message);
    }
}
