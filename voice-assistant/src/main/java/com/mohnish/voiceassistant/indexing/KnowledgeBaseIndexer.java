package com.mohnish.voiceassistant.indexing;

import com.mohnish.voiceassistant.document.DocumentChunk;
import com.mohnish.voiceassistant.document.DocumentParser;
import com.mohnish.voiceassistant.document.TextChunker;
import com.mohnish.voiceassistant.embedding.EmbeddingGenerator;
import com.mohnish.voiceassistant.vectordb.ChromaDBClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KnowledgeBaseIndexer {
    private static final Logger logger = LoggerFactory.getLogger(KnowledgeBaseIndexer.class);
    
    private final DocumentParser parser;
    private final TextChunker chunker;
    private final EmbeddingGenerator embeddingGenerator;
    private final ChromaDBClient chromaClient;
    private final String collectionName;
    
    public KnowledgeBaseIndexer(String chromaUrl, String ollamaUrl, String collectionName) {
        this.parser = new DocumentParser();
        this.chunker = new TextChunker();
        this.embeddingGenerator = new EmbeddingGenerator(ollamaUrl);
        this.chromaClient = new ChromaDBClient(chromaUrl);
        this.collectionName = collectionName;
        
        logger.info("Knowledge Base Indexer initialized");
        logger.info("Collection: {}", collectionName);
    }
    
    /**
     * Initialize the collection (create if doesn't exist)
     */
    public void initializeCollection() throws Exception {
        logger.info("Initializing collection: {}", collectionName);
        chromaClient.createCollection(collectionName);
        logger.info("✅ Collection ready");
    }
    
    /**
     * Index a single book
     */
    public IndexingResult indexBook(File bookFile) throws Exception {
        logger.info("Starting to index book: {}", bookFile.getName());
        long startTime = System.currentTimeMillis();
        
        IndexingResult result = new IndexingResult(bookFile.getName());
        
        try {
            // Step 1: Extract text
            logger.info("Step 1/4: Extracting text...");
            String text = parser.extractText(bookFile);
            result.setCharactersExtracted(text.length());
            logger.info("✅ Extracted {} characters", text.length());
            
            // Step 2: Chunk text
            logger.info("Step 2/4: Chunking text...");
            List<DocumentChunk> chunks = chunker.chunkText(text, bookFile.getName());
            result.setChunksCreated(chunks.size());
            logger.info("✅ Created {} chunks", chunks.size());
            
            // Step 3: Generate embeddings
            logger.info("Step 3/4: Generating embeddings...");
            List<List<Double>> embeddings = embeddingGenerator.generateForChunks(chunks);
            result.setEmbeddingsGenerated(embeddings.size());
            logger.info("✅ Generated {} embeddings", embeddings.size());
            
            // Step 4: Store in ChromaDB
            logger.info("Step 4/4: Storing in ChromaDB...");
            storeChunksInChroma(chunks, embeddings);
            result.setChunksStored(chunks.size());
            logger.info("✅ Stored {} chunks in database", chunks.size());
            
            long duration = System.currentTimeMillis() - startTime;
            result.setDurationMs(duration);
            result.setSuccess(true);
            
            logger.info("✅ Successfully indexed book in {}ms", duration);
            return result;
            
        } catch (Exception e) {
            logger.error("Failed to index book: {}", bookFile.getName(), e);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
            throw e;
        }
    }
    
    /**
     * Store chunks with their embeddings in ChromaDB
     */
    private void storeChunksInChroma(List<DocumentChunk> chunks, List<List<Double>> embeddings) 
            throws Exception {
        
        if (chunks.size() != embeddings.size()) {
            throw new IllegalArgumentException("Chunks and embeddings size mismatch");
        }
        
        // Prepare data for ChromaDB
        List<String> ids = new ArrayList<>();
        List<String> documents = new ArrayList<>();
        List<Map<String, String>> metadatas = new ArrayList<>();
        
        for (int i = 0; i < chunks.size(); i++) {
            DocumentChunk chunk = chunks.get(i);
            
            ids.add(chunk.getId());
            documents.add(chunk.getText());
            
            Map<String, String> metadata = new HashMap<>();
            metadata.put("source", chunk.getSourceFile());
            metadata.put("chunk_index", String.valueOf(chunk.getChunkIndex()));
            metadatas.add(metadata);
        }
        
        // Store in ChromaDB
        chromaClient.addDocuments(collectionName, ids, embeddings, documents, metadatas);
    }
    
    /**
     * Index multiple books
     */
    public List<IndexingResult> indexBooks(List<File> bookFiles) throws Exception {
        logger.info("Starting to index {} books", bookFiles.size());
        
        List<IndexingResult> results = new ArrayList<>();
        
        for (int i = 0; i < bookFiles.size(); i++) {
            File bookFile = bookFiles.get(i);
            logger.info("\n=== Indexing book {}/{}: {} ===", 
                i + 1, bookFiles.size(), bookFile.getName());
            
            try {
                IndexingResult result = indexBook(bookFile);
                results.add(result);
                
                // Small delay between books
                if (i < bookFiles.size() - 1) {
                    Thread.sleep(1000);
                }
                
            } catch (Exception e) {
                logger.error("Failed to index book: {}", bookFile.getName(), e);
                IndexingResult failedResult = new IndexingResult(bookFile.getName());
                failedResult.setSuccess(false);
                failedResult.setErrorMessage(e.getMessage());
                results.add(failedResult);
            }
        }
        
        logger.info("\n✅ Indexing complete!");
        logger.info("Successfully indexed: {}/{}", 
            results.stream().filter(IndexingResult::isSuccess).count(),
            results.size());
        
        return results;
    }
    
    /**
     * Index all books in a directory
     */
    public List<IndexingResult> indexDirectory(String directoryPath) throws Exception {
        File directory = new File(directoryPath);
        
        if (!directory.exists() || !directory.isDirectory()) {
            throw new IllegalArgumentException("Directory not found: " + directoryPath);
        }
        
        File[] files = directory.listFiles((dir, name) -> 
            name.toLowerCase().endsWith(".pdf"));
        
        if (files == null || files.length == 0) {
            logger.warn("No PDF files found in directory: {}", directoryPath);
            return new ArrayList<>();
        }
        
        List<File> bookFiles = List.of(files);
        logger.info("Found {} PDF files to index", bookFiles.size());
        
        return indexBooks(bookFiles);
    }
    
    /**
     * Get collection statistics
     */
    public Map<String, Object> getCollectionStats() {
        Map<String, Object> stats = new HashMap<>();
        try {
            // This would query ChromaDB for collection info
            stats.put("collection", collectionName);
            stats.put("status", "active");
        } catch (Exception e) {
            logger.error("Failed to get stats", e);
        }
        return stats;
    }
}