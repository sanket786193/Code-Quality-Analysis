package com.sanket.AI.Code.Review.Platform.ai.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanket.AI.Code.Review.Platform.ai.exception.ResponseParsingException;
import com.sanket.AI.Code.Review.Platform.ai.model.AiCodeReviewResult;
import com.sanket.AI.Code.Review.Platform.ai.model.AiReviewFindingModel;
import com.sanket.AI.Code.Review.Platform.ai.model.AiReviewRecommendation;
import com.sanket.AI.Code.Review.Platform.review.entity.FindingSeverity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class AiResponseParser {

    private final ObjectMapper objectMapper;

    public AiResponseParser(@Qualifier("aiObjectMapper") ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Parses raw LLM text into a strongly-typed AiCodeReviewResult object.
     */
    public AiCodeReviewResult parse(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            throw new ResponseParsingException("Cannot parse empty or null AI response.");
        }

        try {
            String cleanedJson = extractJsonPayload(rawResponse);
            JsonNode rootNode = objectMapper.readTree(cleanedJson);

            String summary = rootNode.path("summary").asText("Automated AI Code Review completed.");
            double overallScore = rootNode.path("overallScore").asDouble(80.0);
            double confidenceScore = rootNode.path("confidenceScore").asDouble(0.85);

            String recStr = rootNode.path("recommendation").asText("APPROVE_WITH_CHANGES");
            AiReviewRecommendation recommendation = parseRecommendation(recStr);

            List<AiReviewFindingModel> findings = new ArrayList<>();
            JsonNode findingsNode = rootNode.path("findings");

            if (findingsNode.isArray()) {
                for (JsonNode fNode : findingsNode) {
                    AiReviewFindingModel finding = mapFindingNode(fNode);
                    findings.add(finding);
                }
            }

            return AiCodeReviewResult.builder()
                    .summary(summary)
                    .overallScore(overallScore)
                    .recommendation(recommendation)
                    .confidenceScore(confidenceScore)
                    .findings(findings)
                    .rawOutput(rawResponse)
                    .build();

        } catch (ResponseParsingException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to parse AI JSON response: {}", rawResponse, ex);
            throw new ResponseParsingException("Failed to parse AI response into structured review findings: " + ex.getMessage(), ex);
        }
    }

    private AiReviewFindingModel mapFindingNode(JsonNode fNode) {
        String issueType = fNode.path("issueType").asText("CODE_SMELL");
        String severityStr = fNode.path("severity").asText("MEDIUM");
        FindingSeverity severity = parseSeverity(severityStr);

        String fileName = fNode.path("fileName").asText("UnknownFile");
        int lineNumber = fNode.path("lineNumber").asInt(1);
        String explanation = fNode.path("explanation").asText("Identified code concern.");
        String suggestedFix = fNode.path("suggestedFix").asText("");
        double confidence = fNode.path("confidenceScore").asDouble(0.80);
        String summary = fNode.path("summary").asText(issueType + " in " + fileName);
        String snippetBefore = fNode.path("snippetBefore").asText("");
        String snippetAfter = fNode.path("snippetAfter").asText("");
        String ruleKey = fNode.path("ruleKey").asText(null);

        return AiReviewFindingModel.builder()
                .issueType(issueType)
                .severity(severity)
                .fileName(fileName)
                .lineNumber(lineNumber)
                .explanation(explanation)
                .suggestedFix(suggestedFix)
                .confidenceScore(confidence)
                .summary(summary)
                .snippetBefore(snippetBefore)
                .snippetAfter(snippetAfter)
                .ruleKey(ruleKey)
                .build();
    }

    private String extractJsonPayload(String text) {
        String trimmed = text.trim();

        // Strip markdown ```json ... ``` code blocks if present
        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.substring(7);
        } else if (trimmed.startsWith("```")) {
            trimmed = trimmed.substring(3);
        }

        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.length() - 3);
        }

        trimmed = trimmed.trim();

        // Extract substring between first '{' and last '}'
        int firstBrace = trimmed.indexOf('{');
        int lastBrace = trimmed.lastIndexOf('}');
        if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
            return trimmed.substring(firstBrace, lastBrace + 1);
        }

        return trimmed;
    }

    private AiReviewRecommendation parseRecommendation(String rec) {
        if (rec == null) return AiReviewRecommendation.APPROVE_WITH_CHANGES;
        try {
            return AiReviewRecommendation.valueOf(rec.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            if (rec.toUpperCase().contains("REJECT")) return AiReviewRecommendation.REJECT;
            if (rec.toUpperCase().contains("APPROVE") && rec.toUpperCase().contains("CHANGE")) return AiReviewRecommendation.APPROVE_WITH_CHANGES;
            if (rec.toUpperCase().contains("APPROVE")) return AiReviewRecommendation.APPROVE;
            return AiReviewRecommendation.APPROVE_WITH_CHANGES;
        }
    }

    private FindingSeverity parseSeverity(String sev) {
        if (sev == null) return FindingSeverity.MEDIUM;
        try {
            return FindingSeverity.valueOf(sev.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return FindingSeverity.MEDIUM;
        }
    }
}
