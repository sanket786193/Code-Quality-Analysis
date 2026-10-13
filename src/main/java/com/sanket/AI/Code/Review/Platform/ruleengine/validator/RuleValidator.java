package com.sanket.AI.Code.Review.Platform.ruleengine.validator;

import com.sanket.AI.Code.Review.Platform.exception.InvalidRuleCategoryException;
import com.sanket.AI.Code.Review.Platform.exception.InvalidSeverityException;
import com.sanket.AI.Code.Review.Platform.exception.RuleEvaluationException;
import com.sanket.AI.Code.Review.Platform.ruleengine.dto.request.CreateRuleRequest;
import com.sanket.AI.Code.Review.Platform.ruleengine.dto.request.UpdateRuleRequest;
import com.sanket.AI.Code.Review.Platform.ruleengine.entity.RuleCategory;
import com.sanket.AI.Code.Review.Platform.ruleengine.entity.RuleSeverity;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Component
public class RuleValidator {

    public void validateCreateRequest(CreateRuleRequest request) {
        if (request.getCategory() == null) {
            throw new InvalidRuleCategoryException("Rule category cannot be null");
        }
        if (request.getSeverity() == null) {
            throw new InvalidSeverityException("Rule severity cannot be null");
        }
        validateRegex(request.getRegexPattern(), "regexPattern");
        if (request.getNegativePattern() != null && !request.getNegativePattern().isBlank()) {
            validateRegex(request.getNegativePattern(), "negativePattern");
        }
    }

    public void validateUpdateRequest(UpdateRuleRequest request) {
        if (request.getCategory() == null) {
            throw new InvalidRuleCategoryException("Rule category cannot be null");
        }
        if (request.getSeverity() == null) {
            throw new InvalidSeverityException("Rule severity cannot be null");
        }
        validateRegex(request.getRegexPattern(), "regexPattern");
        if (request.getNegativePattern() != null && !request.getNegativePattern().isBlank()) {
            validateRegex(request.getNegativePattern(), "negativePattern");
        }
    }

    public void validateRegex(String regex, String fieldName) {
        if (regex == null || regex.isBlank()) {
            throw new RuleEvaluationException("Pattern expression cannot be blank for field: " + fieldName);
        }
        try {
            Pattern.compile(regex);
        } catch (PatternSyntaxException ex) {
            throw new RuleEvaluationException("Invalid regular expression in '" + fieldName + "': " + ex.getDescription(), ex);
        }
    }
}
