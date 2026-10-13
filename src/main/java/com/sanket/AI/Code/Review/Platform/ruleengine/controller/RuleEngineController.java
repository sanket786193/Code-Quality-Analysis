package com.sanket.AI.Code.Review.Platform.ruleengine.controller;

import com.sanket.AI.Code.Review.Platform.ruleengine.dto.request.CreateRuleRequest;
import com.sanket.AI.Code.Review.Platform.ruleengine.dto.request.EvaluateCodeRequest;
import com.sanket.AI.Code.Review.Platform.ruleengine.dto.request.UpdateRuleRequest;
import com.sanket.AI.Code.Review.Platform.ruleengine.dto.response.RuleDefinitionResponse;
import com.sanket.AI.Code.Review.Platform.ruleengine.dto.response.RuleEvaluationResponse;
import com.sanket.AI.Code.Review.Platform.ruleengine.entity.RuleCategory;
import com.sanket.AI.Code.Review.Platform.ruleengine.service.RuleEngineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rules")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Rule Engine", description = "Endpoints for configuring coding standards and evaluating rules")
@SecurityRequirement(name = "bearerAuth")
public class RuleEngineController {

    private final RuleEngineService ruleEngineService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create Governance Rule (Admin)", description = "Adds a new organization coding standard rule")
    public ResponseEntity<RuleDefinitionResponse> createRule(@Valid @RequestBody CreateRuleRequest request) {
        RuleDefinitionResponse response = ruleEngineService.createRule(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DEVELOPER', 'TEAM_LEAD', 'ADMIN')")
    @Operation(summary = "List All Rules", description = "Retrieves all configured coding standards across all categories")
    public ResponseEntity<List<RuleDefinitionResponse>> getAllRules() {
        return ResponseEntity.ok(ruleEngineService.getAllRules());
    }

    @GetMapping("/category/{category}")
    @PreAuthorize("hasAnyRole('DEVELOPER', 'TEAM_LEAD', 'ADMIN')")
    @Operation(summary = "List Rules by Category", description = "Filters rules by specific category (e.g. EXCEPTION_HANDLING, SECURITY_RULES)")
    public ResponseEntity<List<RuleDefinitionResponse>> getRulesByCategory(@PathVariable RuleCategory category) {
        return ResponseEntity.ok(ruleEngineService.getRulesByCategory(category));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DEVELOPER', 'TEAM_LEAD', 'ADMIN')")
    @Operation(summary = "Get Rule by ID", description = "Retrieves details for a single rule definition")
    public ResponseEntity<RuleDefinitionResponse> getRuleById(@PathVariable Long id) {
        return ResponseEntity.ok(ruleEngineService.getRuleById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update Rule (Admin)", description = "Modifies regex pattern, severity, or remediation details of a rule")
    public ResponseEntity<RuleDefinitionResponse> updateRule(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRuleRequest request
    ) {
        return ResponseEntity.ok(ruleEngineService.updateRule(id, request));
    }

    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Toggle Rule Active Status (Admin)", description = "Enables or disables rule evaluation globally")
    public ResponseEntity<RuleDefinitionResponse> toggleRule(
            @PathVariable Long id,
            @RequestParam boolean enabled
    ) {
        return ResponseEntity.ok(ruleEngineService.toggleRule(id, enabled));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete Rule (Admin)", description = "Removes a rule definition from the platform")
    public ResponseEntity<Map<String, String>> deleteRule(@PathVariable Long id) {
        ruleEngineService.deleteRule(id);
        return ResponseEntity.ok(Map.of("message", "Rule successfully deleted"));
    }

    @PostMapping("/evaluate")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Evaluate Code Snippet", description = "Evaluates raw code snippet or file contents against active rules")
    public ResponseEntity<RuleEvaluationResponse> evaluateCode(@Valid @RequestBody EvaluateCodeRequest request) {
        return ResponseEntity.ok(ruleEngineService.evaluateSnippet(request));
    }
}
