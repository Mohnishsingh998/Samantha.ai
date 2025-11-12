package com.mohnish.voiceassistant.document;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Chunk multiple documents in batch
 */
public class BatchChunker {
    private static final Logger logger = LoggerFactory.getLogger(BatchChunker.class);
    
    private final DocumentParser parser;
    private final TextChunker chunker;
    
    public BatchChunker() {
        this.parser = new DocumentParser();
        this.chunker = new TextChunker(
            500,  // chunk size
            50,   // overlap
            TextChunker.ChunkingStrategy.SENTENCE_BOUNDARY
        );
    }
    
    public BatchChunker(TextChunker chunker) {
        this.parser = new DocumentParser();
        this.chunker = chunker;
    }
    
    /**
     * Process all documents in a directory
     */
    public List<DocumentChunk> processDirectory(File directory) {
        List<DocumentChunk> allChunks = new ArrayList<>();
        
        if (!directory.exists() || !directory.isDirectory()) {
            logger.error("Directory not found: {}", directory.getAbsolutePath());
            return allChunks;
        }
        
        File[] files = directory.listFiles((dir, name) -> 
            name.toLowerCase().endsWith(".pdf") || name.toLowerCase().endsWith(".txt")
        );
        
        if (files == null || files.length == 0) {
            logger.warn("No documents found in: {}", directory.getAbsolutePath());
            return allChunks;
        }
        
        logger.info("Processing {} documents for chunking", files.length);
        
        int totalChunks = 0;
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            logger.info("[{}/{}] Chunking: {}", i + 1, files.length, file.getName());
            
            try {
                // Extract text
                String text = parser.extractText(file);
                DocumentMetadata metadata = parser.getMetadata(file);
                
                // Chunk text
                List<DocumentChunk> chunks = chunker.chunkDocument(
                    text,
                    file.getName(),
                    metadata.getTitle()
                );
                
                allChunks.addAll(chunks);
                totalChunks += chunks.size();
                
                logger.info("✅ Created {} chunks from {}", chunks.size(), file.getName());
                
            } catch (Exception e) {
                logger.error("❌ Failed to chunk: {}", file.getName(), e);
            }
        }
        
        logger.info("Batch chunking complete: {} total chunks from {} documents", 
            totalChunks, files.length);
        
        return allChunks;
    }
    
    /**
     * Get chunking statistics
     */
    public ChunkingStats getStatistics(List<DocumentChunk> chunks) {
        if (chunks.isEmpty()) {
            return new ChunkingStats();
        }
        
        int totalWords = 0;
        int totalChars = 0;
        int minWords = Integer.MAX_VALUE;
        int maxWords = 0;
        
        for (DocumentChunk chunk : chunks) {
            int words = chunk.getWordCount();
            int chars = chunk.getCharCount();
            
            totalWords += words;
            totalChars += chars;
            minWords = Math.min(minWords, words);
            maxWords = Math.max(maxWords, words);
        }
        
        ChunkingStats stats = new ChunkingStats();
        stats.totalChunks = chunks.size();
        stats.totalWords = totalWords;
        stats.totalChars = totalChars;
        stats.minWords = minWords;
        stats.maxWords = maxWords;
        stats.avgWords = totalWords / chunks.size();
        stats.avgChars = totalChars / chunks.size();
        
        return stats;
    }
    
    /**
     * Chunking statistics
     */
    public static class ChunkingStats {
        public int totalChunks;
        public int totalWords;
        public int totalChars;
        public int minWords;
        public int maxWords;
        public int avgWords;
        public int avgChars;
        
        @Override
        public String toString() {
            return String.format(
                "ChunkingStats{chunks=%d, words=%d, avgWords=%d (min=%d, max=%d)}",
                totalChunks, totalWords, avgWords, minWords, maxWords
            );
        }
    }
}