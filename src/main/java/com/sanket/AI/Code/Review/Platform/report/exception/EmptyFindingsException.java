package com.sanket.AI.Code.Review.Platform.report.exception;

public class EmptyFindingsException extends RuntimeException {
    public EmptyFindingsException(String message) {
        super(message);
    }

    public EmptyFindingsException(String message, Throwable cause) {
        super(message, cause);
    }
}
