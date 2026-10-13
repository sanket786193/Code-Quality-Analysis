package com.sanket.AI.Code.Review.Platform.ai.rag;

import com.sanket.AI.Code.Review.Platform.ai.model.KnowledgeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KnowledgeDocumentRepository extends JpaRepository<KnowledgeDocument, Long> {

    List<KnowledgeDocument> findByProjectId(Long projectId);

    List<KnowledgeDocument> findByLanguageIgnoreCase(String language);

    List<KnowledgeDocument> findByProjectIdAndLanguageIgnoreCase(Long projectId, String language);

    /**
     * PGVector cosine similarity retrieval.
     * When pgvector extension is enabled in PostgreSQL:
     * 1 - (embedding_col <=> cast(:queryVector as vector)) gives cosine similarity.
     */
    @Query(value = """
        SELECT kd.*, 
               (1 - (kd.embedding::vector <=> cast(:queryVector as vector))) AS similarity_score
        FROM ai_knowledge_documents kd
        WHERE (kd.project_id IS NULL OR kd.project_id = :projectId)
          AND (:language IS NULL OR LOWER(kd.language) = LOWER(:language) OR LOWER(kd.language) = 'all')
          AND kd.embedding IS NOT NULL
        ORDER BY kd.embedding::vector <=> cast(:queryVector as vector) ASC
        LIMIT :topK
        """, nativeQuery = true)
    List<KnowledgeDocument> searchSimilarDocumentsPgVector(
            @Param("queryVector") String queryVector,
            @Param("projectId") Long projectId,
            @Param("language") String language,
            @Param("topK") int topK
    );
}
