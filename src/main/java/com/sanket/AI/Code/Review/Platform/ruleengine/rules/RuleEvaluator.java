package com.sanket.AI.Code.Review.Platform.ruleengine.rules;

import com.sanket.AI.Code.Review.Platform.ruleengine.entity.RuleDefinition;
import com.sanket.AI.Code.Review.Platform.ruleengine.entity.RuleViolation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * High-performance regex and AST pattern evaluator.
 * Matches active rule definitions against source file lines.
 */
@Component
@Slf4j
public class RuleEvaluator {

    public List<RuleViolation> evaluate(List<RuleDefinition> rules, String filePath, String content) {
        List<RuleViolation> violations = new ArrayList<>();
        if (rules == null || rules.isEmpty() || content == null || content.isBlank()) {
            return violations;
        }

        String[] lines = content.split("\\r?\\n");

        for (RuleDefinition rule : rules) {
            if (!Boolean.TRUE.equals(rule.getEnabled())) {
                continue;
            }

            try {
                Pattern positivePattern = Pattern.compile(rule.getRegexPattern(), Pattern.MULTILINE);
                Pattern negativePattern = (rule.getNegativePattern() != null && !rule.getNegativePattern().isBlank())
                        ? Pattern.compile(rule.getNegativePattern(), Pattern.MULTILINE)
                        : null;

                // Line-by-line scanning with line number tracking
                for (int i = 0; i < lines.length; i++) {
                    String line = lines[i];
                    String trimmed = line.trim();

                    // Skip comment lines
                    if (trimmed.startsWith("//") || trimmed.startsWith("/*") || trimmed.startsWith("*")) {
                        continue;
                    }

                    Matcher posMatcher = positivePattern.matcher(line);
                    if (posMatcher.find()) {
                        // If negative pattern exists and matches line/context, skip (exception satisfied)
                        if (negativePattern != null && negativePattern.matcher(line).find()) {
                            continue;
                        }

                        RuleViolation violation = RuleViolation.builder()
                                .ruleKey(rule.getRuleKey())
                                .ruleName(rule.getName())
                                .category(rule.getCategory())
                                .severity(rule.getSeverity())
                                .filePath(filePath)
                                .lineNumber(i + 1)
                                .matchedSnippet(line.trim())
                                .message(rule.getDescription())
                                .remediation(rule.getRemediation())
                                .createdAt(LocalDateTime.now())
                                .build();

                        violations.add(violation);
                    }
                }
            } catch (Exception ex) {
                log.error("Error evaluating rule {} against file {}: {}", rule.getRuleKey(), filePath, ex.getMessage());
            }
        }

        return violations;
    }
}
