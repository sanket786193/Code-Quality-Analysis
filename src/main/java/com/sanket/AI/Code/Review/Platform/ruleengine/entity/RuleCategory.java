package com.sanket.AI.Code.Review.Platform.ruleengine.entity;

public enum RuleCategory {
    EXCEPTION_HANDLING("Exception Handling"),
    NULL_SAFETY("Null Safety"),
    TRANSACTION_RULES("Transaction Rules"),
    VALIDATION_RULES("Validation Rules"),
    API_DESIGN("API Design"),
    LOGGING_RULES("Logging Rules"),
    SECURITY_RULES("Security Rules"),
    AUTH_RULES("Authentication and Authorization Rules"),
    DATABASE_RULES("Database Rules"),
    PERFORMANCE_RULES("Performance Rules"),
    CODE_STYLE("Code Style"),
    COMPLEXITY_RULES("Complexity Rules"),
    ARCHITECTURE_RULES("Architecture Rules"),
    DTO_MAPPING_RULES("DTO and Mapping Rules"),
    CONCURRENCY_RULES("Concurrency Rules"),
    TESTING_RULES("Testing Rules"),
    DOCUMENTATION_RULES("Documentation Rules"),
    DEPENDENCY_RULES("Dependency Rules"),
    CONFIGURATION_RULES("Configuration Rules"),
    BUSINESS_RULES("Business Rules");

    private final String displayName;

    RuleCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
