package com.sanket.AI.Code.Review.Platform.ai.service;

import com.sanket.AI.Code.Review.Platform.ai.dto.request.AiReviewJobRequest;
import com.sanket.AI.Code.Review.Platform.ai.dto.response.AiFindingResponse;
import com.sanket.AI.Code.Review.Platform.ai.dto.response.AiReviewJobResponse;
import com.sanket.AI.Code.Review.Platform.ai.model.AiCodeReviewResult;
import com.sanket.AI.Code.Review.Platform.ai.ollama.LlmExecutionService;
import com.sanket.AI.Code.Review.Platform.ai.parser.AiResponseParser;
import com.sanket.AI.Code.Review.Platform.ai.prompt.ReviewPromptBuilder;
import com.sanket.AI.Code.Review.Platform.ai.rag.RetrieverService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiReviewOrchestrationServiceImpl implements AiReviewOrchestrationService {

    private final ReviewPromptBuilder promptBuilder;
    private final RetrieverService retrieverService;
    private final LlmExecutionService llmExecutionService;
    private final AiResponseParser responseParser;

    @Override
    public AiReviewJobResponse executeAiReview(AiReviewJobRequest request) {
        log.info("Starting AI review orchestration for Job Ref: {}, PR: '{}'", 
                request.getJobReference(), request.getPullRequestTitle());
        long startTime = System.currentTimeMillis();

        // Step 1: Retrieve similar context and team conventions using PGVector RAG
        String ragQueryText = request.getPullRequestTitle() + " " + String.join(" ", request.getChangedFiles());
        List<String> ragContext = retrieverService.retrieveContextStrings(
                request.getProjectId(),
                ragQueryText,
                request.getProgrammingLanguage(),
                4
        );
        log.debug("Retrieved {} RAG context documents for review job", ragContext.size());

        // Step 2: Build tailored System and User Prompts
        String systemPrompt = promptBuilder.buildSystemPrompt(request.getProgrammingLanguage());
        String userPrompt = promptBuilder.buildUserPrompt(request, ragContext);

        // Step 3: Execute prompt against the configured LLM (Ollama)
        String rawLlmResponse = llmExecutionService.executeReviewPrompt(systemPrompt, userPrompt);

        // Step 4: Parse structured findings and metadata
        AiCodeReviewResult reviewResult = responseParser.parse(rawLlmResponse);

        long latency = System.currentTimeMillis() - startTime;
        log.info("AI review orchestration completed for Job Ref: {} in {} ms with {} findings",
                request.getJobReference(), latency, reviewResult.getFindings().size());

        // Step 5: Convert to external response DTO
        List<AiFindingResponse> findingResponses = reviewResult.getFindings().stream()
                .map(f -> AiFindingResponse.builder()
                        .issueType(f.getIssueType())
                        .severity(f.getSeverity())
                        .fileName(f.getFileName())
                        .lineNumber(f.getLineNumber())
                        .explanation(f.getExplanation())
                        .suggestedFix(f.getSuggestedFix())
                        .confidenceScore(f.getConfidenceScore())
                        .summary(f.getSummary())
                        .snippetBefore(f.getSnippetBefore())
                        .snippetAfter(f.getSnippetAfter())
                        .ruleKey(f.getRuleKey())
                        .build())
                .collect(Collectors.toList());

        return AiReviewJobResponse.builder()
                .jobReference(request.getJobReference())
                .reviewJobId(request.getReviewJobId())
                .recommendation(reviewResult.getRecommendation())
                .overallScore(reviewResult.getOverallScore())
                .summary(reviewResult.getSummary())
                .confidenceScore(reviewResult.getConfidenceScore())
                .latencyMs(latency)
                .modelUsed(llmExecutionService.getModelName())
                .findings(findingResponses)
                .reviewedAt(LocalDateTime.now())
                .build();
    }
}
