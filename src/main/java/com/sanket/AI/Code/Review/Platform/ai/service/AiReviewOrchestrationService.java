package com.sanket.AI.Code.Review.Platform.ai.service;

import com.sanket.AI.Code.Review.Platform.ai.dto.request.AiReviewJobRequest;
import com.sanket.AI.Code.Review.Platform.ai.dto.response.AiReviewJobResponse;

public interface AiReviewOrchestrationService {

    /**
     * Orchestrates the complete end-to-end AI code review pipeline:
     * - Vector context retrieval via PGVector
     * - Dynamic prompt construction
     * - LLM invocation via Ollama
     * - Structured output parsing
     * - Findings aggregation and metrics
     */
    AiReviewJobResponse executeAiReview(AiReviewJobRequest request);
}
