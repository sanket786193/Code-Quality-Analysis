package com.sanket.AI.Code.Review.Platform.ai.rag;

import com.sanket.AI.Code.Review.Platform.ai.dto.request.RagQueryRequest;
import com.sanket.AI.Code.Review.Platform.ai.dto.response.RagQueryResult;
import com.sanket.AI.Code.Review.Platform.ai.exception.RetrieverException;
import com.sanket.AI.Code.Review.Platform.ai.model.KnowledgeDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PgVectorRetrieverServiceImpl implements RetrieverService {

    private final KnowledgeDocumentRepository knowledgeDocumentRepository;
    private final EmbeddingService embeddingService;

    @Override
    @Transactional(readOnly = true)
    public List<RagQueryResult> retrieveContext(RagQueryRequest request) {
        if (request == null || request.getQuery() == null || request.getQuery().isBlank()) {
            throw new RetrieverException("RAG query text cannot be empty.");
        }

        try {
            int topK = request.getTopK() != null ? request.getTopK() : 5;
            List<Double> queryEmbedding = embeddingService.generateEmbedding(request.getQuery());
            String queryVectorStr = embeddingService.toVectorString(queryEmbedding);

            List<KnowledgeDocument> matchingDocs;
            try {
                // Attempt native PGVector cosine distance query
                matchingDocs = knowledgeDocumentRepository.searchSimilarDocumentsPgVector(
                        queryVectorStr,
                        request.getProjectId(),
                        request.getLanguage(),
                        topK
                );
            } catch (Exception dbEx) {
                log.warn("PGVector native query failed or extension not active, using in-memory cosine fallback: {}", dbEx.getMessage());
                matchingDocs = fallbackCosineSimilaritySearch(queryEmbedding, request.getProjectId(), request.getLanguage(), topK);
            }

            return matchingDocs.stream()
                    .map(doc -> RagQueryResult.builder()
                            .documentId(doc.getId())
                            .title(doc.getTitle())
                            .category(doc.getCategory())
                            .language(doc.getLanguage())
                            .content(doc.getContent())
                            .ruleKey(doc.getRuleKey())
                            .similarityScore(0.85) // placeholder or calculated
                            .build())
                    .collect(Collectors.toList());

        } catch (Exception ex) {
            log.error("Failed to retrieve RAG context for query '{}': {}", request.getQuery(), ex.getMessage());
            throw new RetrieverException("Vector retrieval error: " + ex.getMessage(), ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> retrieveContextStrings(Long projectId, String query, String language, int topK) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        try {
            RagQueryRequest request = RagQueryRequest.builder()
                    .projectId(projectId)
                    .query(query)
                    .language(language)
                    .topK(topK)
                    .build();

            List<RagQueryResult> results = retrieveContext(request);
            return results.stream()
                    .map(r -> String.format("[%s - %s]: %s", r.getCategory(), r.getTitle(), r.getContent()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Failed retrieving RAG context strings, continuing review without external context: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    @Transactional
    public KnowledgeDocument indexDocument(Long projectId, String title, String category, String language, String content, String ruleKey) {
        try {
            List<Double> embedding = embeddingService.generateEmbedding(content);
            String embeddingStr = embeddingService.toVectorString(embedding);

            KnowledgeDocument doc = KnowledgeDocument.builder()
                    .projectId(projectId)
                    .title(title)
                    .category(category != null ? category : "CODING_STANDARD")
                    .language(language != null ? language : "JAVA")
                    .content(content)
                    .ruleKey(ruleKey)
                    .embeddingJson(embeddingStr)
                    .build();

            return knowledgeDocumentRepository.save(doc);
        } catch (Exception ex) {
            log.error("Failed to index knowledge document '{}': {}", title, ex.getMessage());
            throw new RetrieverException("Failed to index document: " + ex.getMessage(), ex);
        }
    }

    private List<KnowledgeDocument> fallbackCosineSimilaritySearch(List<Double> queryVec, Long projectId, String language, int topK) {
        List<KnowledgeDocument> allDocs = knowledgeDocumentRepository.findAll();
        List<ScoredDoc> scored = new ArrayList<>();

        for (KnowledgeDocument doc : allDocs) {
            if (projectId != null && doc.getProjectId() != null && !projectId.equals(doc.getProjectId())) {
                continue;
            }
            if (language != null && doc.getLanguage() != null 
                    && !doc.getLanguage().equalsIgnoreCase("ALL") 
                    && !doc.getLanguage().equalsIgnoreCase(language)) {
                continue;
            }

            List<Double> docVec = embeddingService.fromVectorString(doc.getEmbeddingJson());
            if (docVec != null && !docVec.isEmpty()) {
                double score = cosineSimilarity(queryVec, docVec);
                scored.add(new ScoredDoc(doc, score));
            }
        }

        return scored.stream()
                .sorted(Comparator.comparingDouble(ScoredDoc::score).reversed())
                .limit(topK)
                .map(ScoredDoc::doc)
                .collect(Collectors.toList());
    }

    private double cosineSimilarity(List<Double> vA, List<Double> vB) {
        if (vA == null || vB == null || vA.isEmpty() || vB.isEmpty() || vA.size() != vB.size()) {
            return 0.0;
        }
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vA.size(); i++) {
            double a = vA.get(i);
            double b = vB.get(i);
            dotProduct += a * b;
            normA += a * a;
            normB += b * b;
        }
        if (normA <= 0.0 || normB <= 0.0) return 0.0;
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private record ScoredDoc(KnowledgeDocument doc, double score) {}
}
