package com.sanket.AI.Code.Review.Platform.ai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.ai.ollama")
@Getter
@Setter
public class OllamaProperties {

    /**
     * Base URL of the Ollama server instance (e.g., http://localhost:11434)
     */
    private String baseUrl = "http://localhost:11434";

    /**
     * Primary chat/completion model for code review (e.g. codellama, llama3.2, qwen2.5-coder)
     */
    private String model = "codellama:13b";

    /**
     * Embedding model used for RAG PGVector indexing (e.g. nomic-embed-text, all-minilm)
     */
    private String embeddingModel = "nomic-embed-text";

    /**
     * Sampling temperature (0.0 to 1.0). Low temperature ensures deterministic, precise code reviews.
     */
    private Double temperature = 0.2;

    /**
     * Top-P nucleus sampling parameter
     */
    private Double topP = 0.9;

    /**
     * Timeout for completion in seconds (code reviews with large diffs can take 30-60s)
     */
    private Integer timeoutSeconds = 120;

    /**
     * Maximum tokens to generate
     */
    private Integer maxTokens = 4096;
}
