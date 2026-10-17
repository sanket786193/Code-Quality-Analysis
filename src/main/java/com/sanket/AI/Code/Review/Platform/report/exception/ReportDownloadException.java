package com.sanket.AI.Code.Review.Platform.report.exception;

public class ReportDownloadException extends RuntimeException {
    public ReportDownloadException(String message) {
        super(message);
    }

    public ReportDownloadException(String message, Throwable cause) {
        super(message, cause);
    }
}
