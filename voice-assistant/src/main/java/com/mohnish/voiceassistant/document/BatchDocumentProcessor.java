package com.mohnish.voiceassistant.document;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Process multiple documents in batch
 */
public class BatchDocumentProcessor {
    private static final Logger logger = LoggerFactory.getLogger(BatchDocumentProcessor.class);
    private final DocumentParser parser;
    
    public BatchDocumentProcessor() {
        this.parser = new DocumentParser();
    }
    
    /**
     * Process all documents in a directory
     */
    public List<ProcessedDocument> processDirectory(File directory) {
        List<ProcessedDocument> results = new ArrayList<>();
        
        if (!directory.exists() || !directory.isDirectory()) {
            logger.error("Directory not found: {}", directory.getAbsolutePath());
            return results;
        }
        
        File[] files = directory.listFiles((dir, name) -> 
            name.toLowerCase().endsWith(".pdf") || name.toLowerCase().endsWith(".txt")
        );
        
        if (files == null || files.length == 0) {
            logger.warn("No documents found in: {}", directory.getAbsolutePath());
            return results;
        }
        
        logger.info("Processing {} documents from: {}", files.length, directory.getName());
        
        int successful = 0;
        int failed = 0;
        
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            logger.info("[{}/{}] Processing: {}", i + 1, files.length, file.getName());
            
            try {
                // Extract metadata
                DocumentMetadata metadata = parser.getMetadata(file);
                
                // Extract text
                String text = parser.extractText(file);
                
                // Create processed document
                ProcessedDocument doc = new ProcessedDocument(file, metadata, text);
                results.add(doc);
                
                successful++;
                logger.info("✅ Successfully processed: {}", file.getName());
                
            } catch (Exception e) {
                failed++;
                logger.error("❌ Failed to process: {}", file.getName(), e);
            }
        }
        
        logger.info("Batch processing complete: {} successful, {} failed", successful, failed);
        return results;
    }
    
    /**
     * Processed document result
     */
    public static class ProcessedDocument {
        private final File file;
        private final DocumentMetadata metadata;
        private final String text;
        
        public ProcessedDocument(File file, DocumentMetadata metadata, String text) {
            this.file = file;
            this.metadata = metadata;
            this.text = text;
        }
        
        public File getFile() { return file; }
        public DocumentMetadata getMetadata() { return metadata; }
        public String getText() { return text; }
        
        public int getWordCount() {
            return text.split("\\s+").length;
        }
        
        public int getCharCount() {
            return text.length();
        }
    }
}