package com.sanket.AI.Code.Review.Platform.ai.rag;

import java.util.List;

public interface EmbeddingService {

    /**
     * Generates a dense vector embedding for the given input text.
     */
    List<Double> generateEmbedding(String text);

    /**
     * Converts a vector of doubles into a formatted vector string for PostgreSQL PGVector (e.g. "[0.12,0.34,...]").
     */
    String toVectorString(List<Double> vector);

    /**
     * Converts a PGVector string into a List of Doubles.
     */
    List<Double> fromVectorString(String vectorString);
}
