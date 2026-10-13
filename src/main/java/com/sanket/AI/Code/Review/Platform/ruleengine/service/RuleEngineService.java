package com.sanket.AI.Code.Review.Platform.ruleengine.service;

import com.sanket.AI.Code.Review.Platform.ruleengine.dto.request.CreateRuleRequest;
import com.sanket.AI.Code.Review.Platform.ruleengine.dto.request.EvaluateCodeRequest;
import com.sanket.AI.Code.Review.Platform.ruleengine.dto.request.UpdateRuleRequest;
import com.sanket.AI.Code.Review.Platform.ruleengine.dto.response.RuleDefinitionResponse;
import com.sanket.AI.Code.Review.Platform.ruleengine.dto.response.RuleEvaluationResponse;
import com.sanket.AI.Code.Review.Platform.ruleengine.entity.RuleCategory;
import com.sanket.AI.Code.Review.Platform.ruleengine.entity.RuleViolation;

import java.util.List;

public interface RuleEngineService {

    RuleDefinitionResponse createRule(CreateRuleRequest request);

    RuleDefinitionResponse updateRule(Long id, UpdateRuleRequest request);

    RuleDefinitionResponse getRuleById(Long id);

    RuleDefinitionResponse getRuleByKey(String ruleKey);

    List<RuleDefinitionResponse> getAllRules();

    List<RuleDefinitionResponse> getRulesByCategory(RuleCategory category);

    RuleDefinitionResponse toggleRule(Long id, boolean enabled);

    void deleteRule(Long id);

    RuleEvaluationResponse evaluateSnippet(EvaluateCodeRequest request);

    List<RuleViolation> evaluateFile(String fileName, String content);
}
