package com.sanket.AI.Code.Review.Platform.ai.prompt;

import com.sanket.AI.Code.Review.Platform.ai.dto.request.AiReviewJobRequest;
import com.sanket.AI.Code.Review.Platform.ai.exception.PromptGenerationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class ReviewPromptBuilder {

    private static final String SYSTEM_PROMPT_TEMPLATE = """
            You are a Senior Principal Software Engineer and Enterprise Application Architect conducting a strict, high-standard automated code review.
            
            YOUR OBJECTIVE:
            Analyze the provided Git diff, pull request metadata, static analysis findings (SonarQube), rule violations, and enterprise guidelines.
            Detect security vulnerabilities, memory/performance leaks, architecture smells, bad practices, and rule non-compliances.
            
            CRITICAL OUTPUT FORMAT REQUIREMENTS:
            1. You MUST respond ONLY with a raw valid JSON object.
            2. Do NOT include markdown code fences like ```json or ```, greetings, conversational text, or explanations outside the JSON.
            3. The JSON structure MUST adhere strictly to the following schema:
            {
              "summary": "Executive summary of the pull request changes and review findings",
              "overallScore": 85.5,
              "recommendation": "APPROVE | APPROVE_WITH_CHANGES | REJECT",
              "confidenceScore": 0.95,
              "findings": [
                {
                  "issueType": "SECURITY | PERFORMANCE | BUG | CODE_SMELL | ARCHITECTURE",
                  "severity": "CRITICAL | HIGH | MEDIUM | LOW | INFO",
                  "fileName": "src/main/java/com/example/UserService.java",
                  "lineNumber": 42,
                  "explanation": "Detailed rationale describing the problem and potential risk",
                  "suggestedFix": "Refactored code snippet or concrete mitigation step",
                  "confidenceScore": 0.90,
                  "summary": "Short title of the finding",
                  "snippetBefore": "original code block with the issue",
                  "snippetAfter": "corrected code block resolving the issue",
                  "ruleKey": "SEC-SQL-01 or null"
                }
              ]
            }
            
            REVIEW GUIDELINES FOR %s:
            - Enforce idiomatic %s practices (e.g. for Java 21: immutability, pattern matching, proper resource management, avoidance of raw exceptions, correct JPA lifecycle).
            - Flag security vulnerabilities: SQL injection, path traversal, hardcoded secrets, unsafe deserialization, missing authorization checks.
            - Flag performance issues: N+1 database queries, unclosed streams/connections, CPU-heavy operations inside loops.
            - Ensure high confidence: Only report real, high-impact issues; do not hallucinate line numbers or files not present in the diff.
            """;

    /**
     * Builds the system prompt tailored to the programming language.
     */
    public String buildSystemPrompt(String language) {
        String lang = (language != null && !language.isBlank()) ? language.toUpperCase() : "JAVA 21";
        return String.format(SYSTEM_PROMPT_TEMPLATE, lang, lang);
    }

    /**
     * Builds the comprehensive user prompt injecting all context and RAG documents.
     */
    public String buildUserPrompt(AiReviewJobRequest request, List<String> ragContextDocuments) {
        if (request == null || request.getGitDiff() == null || request.getGitDiff().isBlank()) {
            throw new PromptGenerationException("Cannot generate prompt: Git diff is missing or empty.");
        }

        try {
            StringBuilder prompt = new StringBuilder();

            prompt.append("=== PULL REQUEST CONTEXT ===\n");
            prompt.append("Title: ").append(request.getPullRequestTitle()).append("\n");
            if (request.getPullRequestDescription() != null && !request.getPullRequestDescription().isBlank()) {
                prompt.append("Description: ").append(request.getPullRequestDescription()).append("\n");
            }
            prompt.append("Source Branch: ").append(request.getBranchName()).append("\n");
            if (request.getTargetBranch() != null) {
                prompt.append("Target Branch: ").append(request.getTargetBranch()).append("\n");
            }
            if (request.getChangedFiles() != null && !request.getChangedFiles().isEmpty()) {
                prompt.append("Changed Files: ").append(String.join(", ", request.getChangedFiles())).append("\n");
            }
            prompt.append("\n");

            // Static Analysis Findings (SonarQube)
            if (request.getStaticAnalysisResults() != null && !request.getStaticAnalysisResults().isBlank()) {
                prompt.append("=== STATIC ANALYSIS FINDINGS (SONARQUBE) ===\n");
                prompt.append(request.getStaticAnalysisResults()).append("\n\n");
            }

            // Rule Engine Violations
            if (request.getRuleViolations() != null && !request.getRuleViolations().isEmpty()) {
                prompt.append("=== CUSTOM RULE VIOLATIONS DETECTED ===\n");
                for (String violation : request.getRuleViolations()) {
                    prompt.append("- ").append(violation).append("\n");
                }
                prompt.append("\n");
            }

            // RAG Context (Company coding standards, architectural rules, team conventions)
            if (ragContextDocuments != null && !ragContextDocuments.isEmpty()) {
                prompt.append("=== RELEVANT ARCHITECTURAL GUIDELINES & CODING STANDARDS (RAG CONTEXT) ===\n");
                for (int i = 0; i < ragContextDocuments.size(); i++) {
                    prompt.append(String.format("[%d] %s\n", i + 1, ragContextDocuments.get(i)));
                }
                prompt.append("\n");
            }

            // Previous Review Context
            if (request.getPreviousReviewContext() != null && !request.getPreviousReviewContext().isBlank()) {
                prompt.append("=== PREVIOUS REVIEW FEEDBACK ON THIS PR ===\n");
                prompt.append(request.getPreviousReviewContext()).append("\n\n");
            }

            // The Git Diff
            prompt.append("=== GIT DIFF TO REVIEW ===\n");
            prompt.append(request.getGitDiff()).append("\n\n");

            prompt.append("Conduct your in-depth review and return the JSON object following the required schema now.");

            return prompt.toString();
        } catch (Exception ex) {
            log.error("Failed to construct user prompt for review job reference: {}", request.getJobReference(), ex);
            throw new PromptGenerationException("Failed to construct AI prompt: " + ex.getMessage(), ex);
        }
    }
}
