package com.sanket.AI.Code.Review.Platform.ai.ollama;

public interface LlmExecutionService {

    /**
     * Executes prompt against the underlying LLM provider and returns the raw response string.
     *
     * @param systemPrompt The persona and formatting guidelines
     * @param userPrompt   The review context, diff, and query
     * @return Raw completion text from the LLM
     */
    String executeReviewPrompt(String systemPrompt, String userPrompt);

    /**
     * Health check verifying whether the LLM provider service and required model are available.
     */
    boolean isModelAvailable();

    /**
     * Returns the name of the active LLM provider (e.g. OLLAMA, OPENAI, ANTHROPIC).
     */
    String getProviderName();

    /**
     * Returns the active model identifier (e.g. codellama:13b).
     */
    String getModelName();
}
