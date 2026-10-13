package com.sanket.AI.Code.Review.Platform.ai.controller;

import com.sanket.AI.Code.Review.Platform.ai.dto.request.AiReviewJobRequest;
import com.sanket.AI.Code.Review.Platform.ai.dto.request.RagQueryRequest;
import com.sanket.AI.Code.Review.Platform.ai.dto.response.AiReviewJobResponse;
import com.sanket.AI.Code.Review.Platform.ai.dto.response.RagQueryResult;
import com.sanket.AI.Code.Review.Platform.ai.model.KnowledgeDocument;
import com.sanket.AI.Code.Review.Platform.ai.ollama.LlmExecutionService;
import com.sanket.AI.Code.Review.Platform.ai.rag.RetrieverService;
import com.sanket.AI.Code.Review.Platform.ai.service.AiReviewOrchestrationService;
import com.sanket.AI.Code.Review.Platform.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AI Review Engine", description = "Endpoints for AI-driven code review, PGVector RAG, and LLM orchestration")
public class AiReviewController {

    private final AiReviewOrchestrationService orchestrationService;
    private final RetrieverService retrieverService;
    private final LlmExecutionService llmExecutionService;

    @PostMapping("/review/execute")
    @PreAuthorize("hasAnyRole('DEVELOPER', 'TEAM_LEAD', 'ADMIN')")
    @Operation(summary = "Execute AI Code Review", description = "Orchestrates RAG retrieval, prompt generation, Ollama LLM execution, and findings parsing")
    public ResponseEntity<ApiResponse<AiReviewJobResponse>> executeReview(
            @Valid @RequestBody AiReviewJobRequest request
    ) {
        AiReviewJobResponse response = orchestrationService.executeAiReview(request);
        return ResponseEntity.ok(ApiResponse.success(response, "AI code review completed successfully"));
    }

    @PostMapping("/rag/index")
    @PreAuthorize("hasAnyRole('TEAM_LEAD', 'ADMIN')")
    @Operation(summary = "Index Coding Guidelines into PGVector", description = "Stores architectural rule or guideline with vector embedding in PGVector")
    public ResponseEntity<ApiResponse<KnowledgeDocument>> indexGuideline(
            @RequestParam(required = false) Long projectId,
            @RequestParam String title,
            @RequestParam(defaultValue = "CODING_STANDARD") String category,
            @RequestParam(defaultValue = "JAVA") String language,
            @RequestParam(required = false) String ruleKey,
            @RequestBody String content
    ) {
        KnowledgeDocument doc = retrieverService.indexDocument(projectId, title, category, language, content, ruleKey);
        return ResponseEntity.ok(ApiResponse.success(doc, "Knowledge document indexed into PGVector successfully"));
    }

    @PostMapping("/rag/query")
    @PreAuthorize("hasAnyRole('DEVELOPER', 'TEAM_LEAD', 'ADMIN')")
    @Operation(summary = "Query PGVector RAG Knowledge Base", description = "Performs vector similarity search against indexed coding guidelines")
    public ResponseEntity<ApiResponse<List<RagQueryResult>>> queryRag(
            @Valid @RequestBody RagQueryRequest request
    ) {
        List<RagQueryResult> results = retrieverService.retrieveContext(request);
        return ResponseEntity.ok(ApiResponse.success(results, "Retrieved RAG context documents successfully"));
    }

    @GetMapping("/health")
    @Operation(summary = "LLM Engine Health Check", description = "Verifies Ollama connectivity and code review model availability")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkLlmHealth() {
        boolean available = llmExecutionService.isModelAvailable();
        Map<String, Object> health = Map.of(
                "provider", llmExecutionService.getProviderName(),
                "model", llmExecutionService.getModelName(),
                "available", available,
                "status", available ? "UP" : "DOWN"
        );
        return ResponseEntity.ok(ApiResponse.success(health, "LLM engine health status"));
    }
}
