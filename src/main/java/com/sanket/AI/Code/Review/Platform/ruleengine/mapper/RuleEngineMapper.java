package com.sanket.AI.Code.Review.Platform.ruleengine.mapper;

import com.sanket.AI.Code.Review.Platform.ruleengine.dto.request.CreateRuleRequest;
import com.sanket.AI.Code.Review.Platform.ruleengine.dto.request.UpdateRuleRequest;
import com.sanket.AI.Code.Review.Platform.ruleengine.dto.response.RuleDefinitionResponse;
import com.sanket.AI.Code.Review.Platform.ruleengine.dto.response.RuleViolationDto;
import com.sanket.AI.Code.Review.Platform.ruleengine.entity.RuleDefinition;
import com.sanket.AI.Code.Review.Platform.ruleengine.entity.RuleViolation;
import org.springframework.stereotype.Component;

@Component
public class RuleEngineMapper {

    public RuleDefinitionResponse toResponse(RuleDefinition entity) {
        if (entity == null) return null;

        return RuleDefinitionResponse.builder()
                .id(entity.getId())
                .ruleKey(entity.getRuleKey())
                .name(entity.getName())
                .category(entity.getCategory())
                .categoryDisplayName(entity.getCategory() != null ? entity.getCategory().getDisplayName() : null)
                .severity(entity.getSeverity())
                .description(entity.getDescription())
                .regexPattern(entity.getRegexPattern())
                .negativePattern(entity.getNegativePattern())
                .remediation(entity.getRemediation())
                .enabled(entity.getEnabled())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public RuleViolationDto toViolationDto(RuleViolation violation) {
        if (violation == null) return null;

        return RuleViolationDto.builder()
                .id(violation.getId())
                .ruleKey(violation.getRuleKey())
                .ruleName(violation.getRuleName())
                .category(violation.getCategory())
                .categoryDisplayName(violation.getCategory() != null ? violation.getCategory().getDisplayName() : null)
                .severity(violation.getSeverity())
                .filePath(violation.getFilePath())
                .lineNumber(violation.getLineNumber())
                .matchedSnippet(violation.getMatchedSnippet())
                .message(violation.getMessage())
                .remediation(violation.getRemediation())
                .createdAt(violation.getCreatedAt())
                .build();
    }

    public RuleDefinition toEntity(CreateRuleRequest request) {
        if (request == null) return null;

        return RuleDefinition.builder()
                .ruleKey(request.getRuleKey())
                .name(request.getName())
                .category(request.getCategory())
                .severity(request.getSeverity())
                .description(request.getDescription())
                .regexPattern(request.getRegexPattern())
                .negativePattern(request.getNegativePattern())
                .remediation(request.getRemediation())
                .enabled(request.getEnabled() != null ? request.getEnabled() : true)
                .build();
    }

    public void updateEntity(UpdateRuleRequest request, RuleDefinition entity) {
        if (request == null || entity == null) return;

        entity.setName(request.getName());
        entity.setCategory(request.getCategory());
        entity.setSeverity(request.getSeverity());
        entity.setDescription(request.getDescription());
        entity.setRegexPattern(request.getRegexPattern());
        entity.setNegativePattern(request.getNegativePattern());
        entity.setRemediation(request.getRemediation());
        if (request.getEnabled() != null) {
            entity.setEnabled(request.getEnabled());
        }
    }
}
