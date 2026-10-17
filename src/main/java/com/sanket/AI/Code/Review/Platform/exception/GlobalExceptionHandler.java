package com.sanket.AI.Code.Review.Platform.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "User Not Found", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmail(DuplicateEmailException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, "Duplicate Email", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(DuplicateUsernameException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateUsername(DuplicateUsernameException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, "Duplicate Username", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(InvalidRoleException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRole(InvalidRoleException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid Role", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedAccess(UnauthorizedAccessException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, "Unauthorized Access", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Invalid Credentials", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Authentication Failed", "Invalid email or password", request.getRequestURI(), null);
    }

    @ExceptionHandler(TokenRefreshException.class)
    public ResponseEntity<ErrorResponse> handleTokenRefreshException(TokenRefreshException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Token Refresh Error", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, "Forbidden", "You do not have permission to access this resource", request.getRequestURI(), null);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(InvalidProjectException.class)
    public ResponseEntity<ErrorResponse> handleInvalidProject(InvalidProjectException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "Project Not Found", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(UnauthorizedProjectAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedProjectAccess(UnauthorizedProjectAccessException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, "Forbidden Project Access", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(RepositoryNotLinkedException.class)
    public ResponseEntity<ErrorResponse> handleRepositoryNotLinked(RepositoryNotLinkedException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Repository Not Linked", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(RepositoryNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleRepositoryNotFound(RepositoryNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "Repository Not Found", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(DuplicateRepositoryMappingException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateRepositoryMapping(DuplicateRepositoryMappingException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, "Duplicate Repository Mapping", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(InvalidWebhookPayloadException.class)
    public ResponseEntity<ErrorResponse> handleInvalidWebhookPayload(InvalidWebhookPayloadException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid Webhook Payload", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(WebhookValidationException.class)
    public ResponseEntity<ErrorResponse> handleWebhookValidation(WebhookValidationException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Webhook Validation Failure", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(GitHubInstallationException.class)
    public ResponseEntity<ErrorResponse> handleGitHubInstallation(GitHubInstallationException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_GATEWAY, "GitHub Integration Error", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(GitHubApiException.class)
    public ResponseEntity<ErrorResponse> handleGitHubApi(GitHubApiException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_GATEWAY, "GitHub API Failure", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(InvalidReviewJobException.class)
    public ResponseEntity<ErrorResponse> handleInvalidReviewJob(InvalidReviewJobException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "Review Job Not Found", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(ReviewJobCreationException.class)
    public ResponseEntity<ErrorResponse> handleReviewJobCreation(ReviewJobCreationException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Review Job Creation Failed", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(PullRequestNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePullRequestNotFound(PullRequestNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "Pull Request Not Found", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(RuleNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleRuleNotFound(RuleNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "Rule Not Found", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(InvalidRuleCategoryException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRuleCategory(InvalidRuleCategoryException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid Rule Category", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(RuleEvaluationException.class)
    public ResponseEntity<ErrorResponse> handleRuleEvaluation(RuleEvaluationException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Rule Evaluation Failure", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(InvalidSeverityException.class)
    public ResponseEntity<ErrorResponse> handleInvalidSeverity(InvalidSeverityException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid Severity Level", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(EmptyRuleSetException.class)
    public ResponseEntity<ErrorResponse> handleEmptyRuleSet(EmptyRuleSetException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Empty Rule Set", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(SonarQubeConnectionException.class)
    public ResponseEntity<ErrorResponse> handleSonarQubeConnection(SonarQubeConnectionException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, "SonarQube Unavailable", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(SonarQubeConfigurationException.class)
    public ResponseEntity<ErrorResponse> handleSonarQubeConfiguration(SonarQubeConfigurationException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid SonarQube Configuration", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(DuplicateProjectNameException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateProjectName(DuplicateProjectNameException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, "Duplicate Project Name", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(OAuth2AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleOAuth2Exception(OAuth2AuthenticationException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "OAuth2 Authentication Error", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid Argument", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation Error", "Invalid input parameters", request.getRequestURI(), errors);
    }

    @ExceptionHandler(com.sanket.AI.Code.Review.Platform.ai.exception.AiServiceException.class)
    public ResponseEntity<ErrorResponse> handleAiServiceException(com.sanket.AI.Code.Review.Platform.ai.exception.AiServiceException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_GATEWAY, "AI Service Failure", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(com.sanket.AI.Code.Review.Platform.ai.exception.ModelUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleModelUnavailableException(com.sanket.AI.Code.Review.Platform.ai.exception.ModelUnavailableException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, "AI Model Unavailable", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(com.sanket.AI.Code.Review.Platform.ai.exception.PromptGenerationException.class)
    public ResponseEntity<ErrorResponse> handlePromptGenerationException(com.sanket.AI.Code.Review.Platform.ai.exception.PromptGenerationException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Prompt Generation Failure", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(com.sanket.AI.Code.Review.Platform.ai.exception.ResponseParsingException.class)
    public ResponseEntity<ErrorResponse> handleResponseParsingException(com.sanket.AI.Code.Review.Platform.ai.exception.ResponseParsingException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, "Response Parsing Failure", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(com.sanket.AI.Code.Review.Platform.ai.exception.EmbeddingException.class)
    public ResponseEntity<ErrorResponse> handleEmbeddingException(com.sanket.AI.Code.Review.Platform.ai.exception.EmbeddingException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Embedding Generation Failure", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(com.sanket.AI.Code.Review.Platform.ai.exception.RetrieverException.class)
    public ResponseEntity<ErrorResponse> handleRetrieverException(com.sanket.AI.Code.Review.Platform.ai.exception.RetrieverException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "RAG Retrieval Failure", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(com.sanket.AI.Code.Review.Platform.report.exception.ReportNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleReportNotFoundException(com.sanket.AI.Code.Review.Platform.report.exception.ReportNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "Report Not Found", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(com.sanket.AI.Code.Review.Platform.report.exception.ReportGenerationException.class)
    public ResponseEntity<ErrorResponse> handleReportGenerationException(com.sanket.AI.Code.Review.Platform.report.exception.ReportGenerationException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Report Generation Failure", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(com.sanket.AI.Code.Review.Platform.report.exception.InvalidReviewSummaryException.class)
    public ResponseEntity<ErrorResponse> handleInvalidReviewSummaryException(com.sanket.AI.Code.Review.Platform.report.exception.InvalidReviewSummaryException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid Review Summary", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(com.sanket.AI.Code.Review.Platform.report.exception.EmptyFindingsException.class)
    public ResponseEntity<ErrorResponse> handleEmptyFindingsException(com.sanket.AI.Code.Review.Platform.report.exception.EmptyFindingsException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Empty Findings", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(com.sanket.AI.Code.Review.Platform.report.exception.ReportDownloadException.class)
    public ResponseEntity<ErrorResponse> handleReportDownloadException(com.sanket.AI.Code.Review.Platform.report.exception.ReportDownloadException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Report Download Failure", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(com.sanket.AI.Code.Review.Platform.report.exception.InvalidTeamLeadDecisionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTeamLeadDecision(com.sanket.AI.Code.Review.Platform.report.exception.InvalidTeamLeadDecisionException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid Review Decision", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(com.sanket.AI.Code.Review.Platform.notification.exception.NotificationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotificationNotFound(com.sanket.AI.Code.Review.Platform.notification.exception.NotificationNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "Notification Not Found", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(com.sanket.AI.Code.Review.Platform.notification.exception.NotificationSendException.class)
    public ResponseEntity<ErrorResponse> handleNotificationSendFailure(com.sanket.AI.Code.Review.Platform.notification.exception.NotificationSendException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Notification Send Failure", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(com.sanket.AI.Code.Review.Platform.notification.exception.InvalidNotificationTypeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidNotificationType(com.sanket.AI.Code.Review.Platform.notification.exception.InvalidNotificationTypeException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid Notification Type", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(com.sanket.AI.Code.Review.Platform.notification.exception.NotificationTemplateNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotificationTemplateNotFound(com.sanket.AI.Code.Review.Platform.notification.exception.NotificationTemplateNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "Notification Template Not Found", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", ex.getMessage(), request.getRequestURI(), null);
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String error, String message, String path, Map<String, String> validationErrors) {
        ErrorResponse response = ErrorResponse.builder()
                .success(false)
                .status(status.value())
                .error(error)
                .message(message)
                .path(path)
                .validationErrors(validationErrors)
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(response, status);
    }
}
