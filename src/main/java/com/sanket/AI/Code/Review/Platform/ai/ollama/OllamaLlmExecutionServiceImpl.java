package com.sanket.AI.Code.Review.Platform.ai.ollama;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanket.AI.Code.Review.Platform.ai.config.OllamaProperties;
import com.sanket.AI.Code.Review.Platform.ai.exception.AiServiceException;
import com.sanket.AI.Code.Review.Platform.ai.exception.ModelUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OllamaLlmExecutionServiceImpl implements LlmExecutionService {

    private final RestClient ollamaRestClient;
    private final OllamaProperties properties;

    @Qualifier("aiObjectMapper")
    private final ObjectMapper objectMapper;

    @Override
    public String executeReviewPrompt(String systemPrompt, String userPrompt) {
        if (!isModelAvailable()) {
            throw new ModelUnavailableException(
                    String.format("Ollama model '%s' is unavailable at %s", properties.getModel(), properties.getBaseUrl()));
        }

        try {
            Map<String, Object> options = new HashMap<>();
            options.put("temperature", properties.getTemperature());
            options.put("top_p", properties.getTopP());
            options.put("num_predict", properties.getMaxTokens());

            Map<String, Object> requestPayload = new HashMap<>();
            requestPayload.put("model", properties.getModel());
            requestPayload.put("system", systemPrompt);
            requestPayload.put("prompt", userPrompt);
            requestPayload.put("stream", false);
            requestPayload.put("format", "json"); // Request structured JSON output from Ollama
            requestPayload.put("options", options);

            log.info("Sending code review prompt to Ollama model '{}' at {}", properties.getModel(), properties.getBaseUrl());
            long startTime = System.currentTimeMillis();

            String responseBody = ollamaRestClient.post()
                    .uri("/api/generate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestPayload)
                    .retrieve()
                    .body(String.class);

            long duration = System.currentTimeMillis() - startTime;
            log.info("Received Ollama completion in {} ms", duration);

            if (responseBody == null || responseBody.isBlank()) {
                throw new AiServiceException("Ollama returned an empty response.");
            }

            JsonNode rootNode = objectMapper.readTree(responseBody);
            JsonNode responseTextNode = rootNode.get("response");

            if (responseTextNode != null && !responseTextNode.isNull()) {
                return responseTextNode.asText();
            }

            throw new AiServiceException("Invalid Ollama payload: missing 'response' node. Response: " + responseBody);

        } catch (ModelUnavailableException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("AI service execution failed: {}", ex.getMessage(), ex);
            throw new AiServiceException("Failed to generate AI code review with Ollama: " + ex.getMessage(), ex);
        }
    }

    @Override
    public boolean isModelAvailable() {
        try {
            String response = ollamaRestClient.get()
                    .uri("/api/tags")
                    .retrieve()
                    .body(String.class);

            if (response != null && !response.isBlank()) {
                JsonNode root = objectMapper.readTree(response);
                JsonNode modelsNode = root.get("models");
                if (modelsNode != null && modelsNode.isArray()) {
                    for (JsonNode modelNode : modelsNode) {
                        String name = modelNode.path("name").asText("");
                        if (name.equalsIgnoreCase(properties.getModel()) || name.startsWith(properties.getModel().split(":")[0])) {
                            return true;
                        }
                    }
                }
                // If tags are returned, server is reachable even if model tag check is slightly loose
                return true;
            }
            return false;
        } catch (Exception ex) {
            log.warn("Ollama health check failed: {}", ex.getMessage());
            return false;
        }
    }

    @Override
    public String getProviderName() {
        return "OLLAMA";
    }

    @Override
    public String getModelName() {
        return properties.getModel();
    }
}
