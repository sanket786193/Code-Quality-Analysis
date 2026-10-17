package com.sanket.AI.Code.Review.Platform.ai.rag;

import com.sanket.AI.Code.Review.Platform.ai.dto.request.RagQueryRequest;
import com.sanket.AI.Code.Review.Platform.ai.dto.response.RagQueryResult;
import com.sanket.AI.Code.Review.Platform.ai.model.KnowledgeDocument;

import java.util.List;

public interface RetrieverService {

    /**
     * Retrieves top-K contextually relevant guidelines and coding rules for a review job.
     */
    List<RagQueryResult> retrieveContext(RagQueryRequest request);

    /**
     * Convenient overload to retrieve formatted string representations of context documents for prompt injection.
     */
    List<String> retrieveContextStrings(Long projectId, String query, String language, int topK);

    /**
     * Stores a new guideline or rule and generates its PGVector embedding.
     */
    KnowledgeDocument indexDocument(Long projectId, String title, String category, String language, String content, String ruleKey);
}
