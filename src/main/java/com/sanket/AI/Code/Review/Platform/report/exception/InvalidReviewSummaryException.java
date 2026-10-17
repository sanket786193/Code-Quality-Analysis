package com.sanket.AI.Code.Review.Platform.report.exception;

public class InvalidReviewSummaryException extends RuntimeException {
    public InvalidReviewSummaryException(String message) {
        super(message);
    }

    public InvalidReviewSummaryException(String message, Throwable cause) {
        super(message, cause);
    }
}
