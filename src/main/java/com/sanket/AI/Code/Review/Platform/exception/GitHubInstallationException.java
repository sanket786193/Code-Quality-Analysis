package com.sanket.AI.Code.Review.Platform.exception;

public class GitHubInstallationException extends RuntimeException {
    public GitHubInstallationException(String message) {
        super(message);
    }

    public GitHubInstallationException(String message, Throwable cause) {
        super(message, cause);
    }
}
