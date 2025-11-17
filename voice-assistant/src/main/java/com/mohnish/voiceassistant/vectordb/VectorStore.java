package com.mohnish.voiceassistant.vectordb;

import com.mohnish.voiceassistant.document.DocumentChunk;
import com.mohnish.voiceassistant.embedding.EmbeddingGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * High-level wrapper around ChromaDBClient for knowledge base operations
 */
public class VectorStore {
    private static final Logger logger = LoggerFactory.getLogger(VectorStore.class);
    
    private final ChromaDBClient chromaClient;
    private final EmbeddingGenerator embeddingGenerator;
    private final String collectionName;
    
    public VectorStore(String chromaUrl, String ollamaUrl, String collectionName) {
        this.chromaClient = new ChromaDBClient(chromaUrl);
        this.embeddingGenerator = new EmbeddingGenerator(ollamaUrl);
        this.collectionName = collectionName;
        
        logger.info("VectorStore initialized for collection: {}", collectionName);
    }
    
    /**
     * Initialize/create the collection
     */
    public void initialize() throws IOException {
        chromaClient.createCollection(collectionName);
        logger.info("Collection initialized: {}", collectionName);
    }
    
    /**
     * Add document chunks with automatic embedding generation
     */
    public void addChunks(List<DocumentChunk> chunks) throws Exception {
        logger.info("Adding {} chunks to vector store", chunks.size());
        
        // Generate embeddings
        List<List<Double>> embeddings = embeddingGenerator.generateForChunks(chunks);
        
        // Prepare data for ChromaDB
        List<String> ids = new ArrayList<>();
        List<String> documents = new ArrayList<>();
        List<Map<String, String>> metadatas = new ArrayList<>();
        
        for (DocumentChunk chunk : chunks) {
            ids.add(chunk.getId());
            documents.add(chunk.getText());
            
            Map<String, String> metadata = new HashMap<>();
            metadata.put("source", chunk.getSourceFile());
            metadata.put("chunk_index", String.valueOf(chunk.getChunkIndex()));
            metadatas.add(metadata);
        }
        
        // Store in ChromaDB
        chromaClient.addDocuments(collectionName, ids, embeddings, documents, metadatas);
        logger.info("✅ Added {} chunks to vector store", chunks.size());
    }
    
    /**
     * Search by query text (generates embedding automatically)
     */
    public List<SearchResult> search(String query, int topK) throws Exception {
        logger.info("Searching for: '{}' (top {})", query, topK);
        
        // Generate embedding for query
        List<Double> queryEmbedding = embeddingGenerator.generateForQuery(query);
        
        // Search ChromaDB
        List<ChromaDBClient.QueryResult> chromaResults = 
            chromaClient.query(collectionName, queryEmbedding, topK);
        
        // Convert to SearchResult
        List<SearchResult> results = new ArrayList<>();
        for (ChromaDBClient.QueryResult chromaResult : chromaResults) {
            SearchResult result = new SearchResult(
                chromaResult.getId(),
                chromaResult.getDocument(),
                chromaResult.getDistance(),
                chromaResult.getMetadata()
            );
            results.add(result);
        }
        
        logger.info("✅ Found {} results", results.size());
        return results;
    }
    
    /**
     * Search by existing embedding (for when you already have the embedding)
     */
    public List<SearchResult> searchByEmbedding(List<Double> embedding, int topK) throws Exception {
        logger.info("Searching by embedding (top {})", topK);
        
        List<ChromaDBClient.QueryResult> chromaResults = 
            chromaClient.query(collectionName, embedding, topK);
        
        List<SearchResult> results = new ArrayList<>();
        for (ChromaDBClient.QueryResult chromaResult : chromaResults) {
            SearchResult result = new SearchResult(
                chromaResult.getId(),
                chromaResult.getDocument(),
                chromaResult.getDistance(),
                chromaResult.getMetadata()
            );
            results.add(result);
        }
        
        return results;
    }
    
    /**
     * List all collections
     */
    public List<String> listCollections() throws IOException {
        return chromaClient.listCollections();
    }
    
    /**
     * Delete the collection
     */
    public void deleteCollection() throws IOException {
        chromaClient.deleteCollection(collectionName);
        logger.info("Collection deleted: {}", collectionName);
    }
    
    /**
     * Test connection
     */
    public boolean testConnection() {
        return chromaClient.testConnection();
    }
    
    /**
     * Search result wrapper class
     */
    public static class SearchResult {
        private final String id;
        private final String text;
        private final double distance;
        private final Map<String, String> metadata;
        
        public SearchResult(String id, String text, double distance, Map<String, String> metadata) {
            this.id = id;
            this.text = text;
            this.distance = distance;
            this.metadata = metadata;
        }
        
        public String getId() { return id; }
        public String getText() { return text; }
        public double getDistance() { return distance; }
        public Map<String, String> getMetadata() { return metadata; }
        
        public String getSource() {
            return metadata.getOrDefault("source", "unknown");
        }
        
        public int getChunkIndex() {
            String indexStr = metadata.getOrDefault("chunk_index", "0");
            try {
                return Integer.parseInt(indexStr);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        
        /**
         * Get relevance score (inverse of distance, normalized)
         */
        public double getRelevanceScore() {
            // Lower distance = higher relevance
            // Convert to 0-1 scale where 1 is most relevant
            double similarity = 1.0 / (1.0 + distance);
    
            return Math.max(0.0, Math.min(1.0, similarity));
        }
        
        @Override
        public String toString() {
            return String.format(
                "SearchResult{source='%s', chunk=%d, distance=%.4f, relevance=%.2f%%}",
                getSource(), getChunkIndex(), distance, getRelevanceScore() * 100
            );
        }
        
        /**
         * Get a preview of the text (first N characters)
         */
        public String getPreview(int maxLength) {
            if (text.length() <= maxLength) {
                return text;
            }
            return text.substring(0, maxLength) + "...";
        }
    }
}