package com.sanket.AI.Code.Review.Platform.ai.rag;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanket.AI.Code.Review.Platform.ai.config.OllamaProperties;
import com.sanket.AI.Code.Review.Platform.ai.exception.EmbeddingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OllamaEmbeddingServiceImpl implements EmbeddingService {

    private final RestClient ollamaRestClient;
    private final OllamaProperties properties;

    @Qualifier("aiObjectMapper")
    private final ObjectMapper objectMapper;

    @Override
    public List<Double> generateEmbedding(String text) {
        if (text == null || text.isBlank()) {
            throw new EmbeddingException("Input text for embedding generation cannot be empty.");
        }

        try {
            Map<String, Object> requestPayload = Map.of(
                    "model", properties.getEmbeddingModel(),
                    "prompt", text.trim()
            );

            String responseBody = ollamaRestClient.post()
                    .uri("/api/embeddings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestPayload)
                    .retrieve()
                    .body(String.class);

            if (responseBody == null || responseBody.isBlank()) {
                throw new EmbeddingException("Ollama returned empty response for embedding request.");
            }

            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode embeddingNode = root.get("embedding");

            if (embeddingNode != null && embeddingNode.isArray()) {
                return objectMapper.convertValue(embeddingNode, new TypeReference<List<Double>>() {});
            }

            throw new EmbeddingException("Response does not contain 'embedding' vector array: " + responseBody);
        } catch (EmbeddingException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to generate embedding with model {}: {}", properties.getEmbeddingModel(), ex.getMessage());
            throw new EmbeddingException("Embedding generation failed: " + ex.getMessage(), ex);
        }
    }

    @Override
    public String toVectorString(List<Double> vector) {
        if (vector == null || vector.isEmpty()) {
            return "[]";
        }
        return vector.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(",", "[", "]"));
    }

    @Override
    public List<Double> fromVectorString(String vectorString) {
        if (vectorString == null || vectorString.isBlank()) {
            return Collections.emptyList();
        }
        try {
            String sanitized = vectorString.trim();
            if (sanitized.startsWith("[") && sanitized.endsWith("]")) {
                sanitized = sanitized.substring(1, sanitized.length() - 1);
            }
            if (sanitized.isBlank()) return Collections.emptyList();

            String[] tokens = sanitized.split(",");
            return java.util.Arrays.stream(tokens)
                    .map(String::trim)
                    .map(Double::parseDouble)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            log.warn("Failed to parse vector string: {}", vectorString, ex);
            return Collections.emptyList();
        }
    }
}
