package com.sanket.AI.Code.Review.Platform.ruleengine.service.impl;

import com.sanket.AI.Code.Review.Platform.exception.EmptyRuleSetException;
import com.sanket.AI.Code.Review.Platform.exception.RuleNotFoundException;
import com.sanket.AI.Code.Review.Platform.ruleengine.dto.request.CreateRuleRequest;
import com.sanket.AI.Code.Review.Platform.ruleengine.dto.request.EvaluateCodeRequest;
import com.sanket.AI.Code.Review.Platform.ruleengine.dto.request.UpdateRuleRequest;
import com.sanket.AI.Code.Review.Platform.ruleengine.dto.response.RuleDefinitionResponse;
import com.sanket.AI.Code.Review.Platform.ruleengine.dto.response.RuleEvaluationResponse;
import com.sanket.AI.Code.Review.Platform.ruleengine.dto.response.RuleViolationDto;
import com.sanket.AI.Code.Review.Platform.ruleengine.entity.RuleCategory;
import com.sanket.AI.Code.Review.Platform.ruleengine.entity.RuleDefinition;
import com.sanket.AI.Code.Review.Platform.ruleengine.entity.RuleSeverity;
import com.sanket.AI.Code.Review.Platform.ruleengine.entity.RuleViolation;
import com.sanket.AI.Code.Review.Platform.ruleengine.mapper.RuleEngineMapper;
import com.sanket.AI.Code.Review.Platform.ruleengine.repository.RuleDefinitionRepository;
import com.sanket.AI.Code.Review.Platform.ruleengine.rules.RuleEvaluator;
import com.sanket.AI.Code.Review.Platform.ruleengine.service.RuleEngineService;
import com.sanket.AI.Code.Review.Platform.ruleengine.validator.RuleValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RuleEngineServiceImpl implements RuleEngineService {

    private final RuleDefinitionRepository ruleRepository;
    private final RuleValidator ruleValidator;
    private final RuleEvaluator ruleEvaluator;
    private final RuleEngineMapper ruleMapper;

    @Override
    @Transactional
    public RuleDefinitionResponse createRule(CreateRuleRequest request) {
        log.info("Creating new governance rule with key: {}", request.getRuleKey());
        ruleValidator.validateCreateRequest(request);

        if (ruleRepository.existsByRuleKey(request.getRuleKey().trim())) {
            throw new IllegalArgumentException("Rule with key '" + request.getRuleKey() + "' already exists.");
        }

        RuleDefinition entity = ruleMapper.toEntity(request);
        RuleDefinition saved = ruleRepository.save(entity);
        return ruleMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public RuleDefinitionResponse updateRule(Long id, UpdateRuleRequest request) {
        log.info("Updating rule ID: {}", id);
        ruleValidator.validateUpdateRequest(request);

        RuleDefinition existing = ruleRepository.findById(id)
                .orElseThrow(() -> new RuleNotFoundException("Rule not found with ID: " + id));

        ruleMapper.updateEntity(request, existing);
        RuleDefinition saved = ruleRepository.save(existing);
        return ruleMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public RuleDefinitionResponse getRuleById(Long id) {
        RuleDefinition rule = ruleRepository.findById(id)
                .orElseThrow(() -> new RuleNotFoundException("Rule not found with ID: " + id));
        return ruleMapper.toResponse(rule);
    }

    @Override
    @Transactional(readOnly = true)
    public RuleDefinitionResponse getRuleByKey(String ruleKey) {
        RuleDefinition rule = ruleRepository.findByRuleKey(ruleKey)
                .orElseThrow(() -> new RuleNotFoundException("Rule not found with key: " + ruleKey));
        return ruleMapper.toResponse(rule);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RuleDefinitionResponse> getAllRules() {
        return ruleRepository.findAll().stream()
                .map(ruleMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RuleDefinitionResponse> getRulesByCategory(RuleCategory category) {
        return ruleRepository.findByCategory(category).stream()
                .map(ruleMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RuleDefinitionResponse toggleRule(Long id, boolean enabled) {
        log.info("Setting rule ID: {} enabled status to: {}", id, enabled);
        RuleDefinition rule = ruleRepository.findById(id)
                .orElseThrow(() -> new RuleNotFoundException("Rule not found with ID: " + id));
        rule.setEnabled(enabled);
        RuleDefinition saved = ruleRepository.save(rule);
        return ruleMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteRule(Long id) {
        log.info("Deleting rule ID: {}", id);
        RuleDefinition rule = ruleRepository.findById(id)
                .orElseThrow(() -> new RuleNotFoundException("Rule not found with ID: " + id));
        ruleRepository.delete(rule);
    }

    @Override
    @Transactional(readOnly = true)
    public RuleEvaluationResponse evaluateSnippet(EvaluateCodeRequest request) {
        log.info("Evaluating code snippet for file: {}", request.getFileName());

        List<RuleDefinition> rules = (request.getCategory() != null)
                ? ruleRepository.findByCategoryAndEnabledTrue(request.getCategory())
                : ruleRepository.findByEnabledTrue();

        if (rules.isEmpty()) {
            throw new EmptyRuleSetException("No enabled rules found for evaluation criteria.");
        }

        List<RuleViolation> violations = ruleEvaluator.evaluate(rules, request.getFileName(), request.getCodeSnippet());

        int critical = (int) violations.stream().filter(v -> v.getSeverity() == RuleSeverity.CRITICAL).count();
        int high = (int) violations.stream().filter(v -> v.getSeverity() == RuleSeverity.HIGH).count();
        int medium = (int) violations.stream().filter(v -> v.getSeverity() == RuleSeverity.MEDIUM).count();
        int low = (int) violations.stream().filter(v -> v.getSeverity() == RuleSeverity.LOW).count();

        List<RuleViolationDto> dtos = violations.stream()
                .map(ruleMapper::toViolationDto)
                .collect(Collectors.toList());

        boolean passed = critical == 0 && high == 0;
        String summary = String.format("Evaluated %d active rules. Detected %d violations (%d critical, %d high, %d medium, %d low).",
                rules.size(), violations.size(), critical, high, medium, low);

        return RuleEvaluationResponse.builder()
                .fileName(request.getFileName())
                .totalViolations(violations.size())
                .passed(passed)
                .criticalCount(critical)
                .highCount(high)
                .mediumCount(medium)
                .lowCount(low)
                .violations(dtos)
                .summary(summary)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RuleViolation> evaluateFile(String fileName, String content) {
        List<RuleDefinition> rules = ruleRepository.findByEnabledTrue();
        return ruleEvaluator.evaluate(rules, fileName, content);
    }
}
