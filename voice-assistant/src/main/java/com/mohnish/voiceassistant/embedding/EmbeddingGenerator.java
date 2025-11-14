package com.mohnish.voiceassistant.embedding;

import com.mohnish.voiceassistant.document.DocumentChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class EmbeddingGenerator {
    private static final Logger logger = LoggerFactory.getLogger(EmbeddingGenerator.class);
    
    private final OllamaEmbeddings ollamaEmbeddings;
    
    public EmbeddingGenerator(String ollamaUrl) {
        this.ollamaEmbeddings = new OllamaEmbeddings(ollamaUrl);
        logger.info("Embedding Generator initialized");
    }
    
    /**
     * Generate embedding for a single document chunk
     */
    public List<Double> generateForChunk(DocumentChunk chunk) throws Exception {
        logger.debug("Generating embedding for chunk: {}", chunk.getId());
        return ollamaEmbeddings.generateEmbedding(chunk.getText());
    }
    
    /**
     * Generate embeddings for multiple chunks
     */
    public List<List<Double>> generateForChunks(List<DocumentChunk> chunks) throws Exception {
        logger.info("Generating embeddings for {} chunks", chunks.size());
        
        List<String> texts = new ArrayList<>();
        for (DocumentChunk chunk : chunks) {
            texts.add(chunk.getText());
        }
        
        return ollamaEmbeddings.generateEmbeddings(texts);
    }
    
    /**
     * Generate embedding for a query (same as chunk but clearer API)
     */
    public List<Double> generateForQuery(String query) throws Exception {
        logger.debug("Generating embedding for query: {}", query);
        return ollamaEmbeddings.generateEmbedding(query);
    }
    
    /**
     * Test if embedding generation is working
     */
    public boolean test() {
        return ollamaEmbeddings.testConnection();
    }
    
    /**
     * Get the dimension size of embeddings
     */
    public int getDimensionSize() throws Exception {
        return ollamaEmbeddings.getDimensionSize();
    }
}