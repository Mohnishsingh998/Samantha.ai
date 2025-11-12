package com.mohnish.voiceassistant.document;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a chunk of text from a document
 */
public class DocumentChunk {
    private String id;                    // Unique identifier
    private String text;                  // Actual text content
    private String sourceFile;            // Origin file name
    private String documentTitle;         // Document title
    private int chunkIndex;               // Position in document
    private int startPosition;            // Character start position
    private int endPosition;              // Character end position
    private Map<String, String> metadata; // Additional metadata
    
    public DocumentChunk(String id, String text, String sourceFile, int chunkIndex) {
        this.id = id;
        this.text = text;
        this.sourceFile = sourceFile;
        this.chunkIndex = chunkIndex;
        this.metadata = new HashMap<>();
    }
    
    // Full constructor
    public DocumentChunk(String id, String text, String sourceFile, 
                        String documentTitle, int chunkIndex, 
                        int startPosition, int endPosition) {
        this.id = id;
        this.text = text;
        this.sourceFile = sourceFile;
        this.documentTitle = documentTitle;
        this.chunkIndex = chunkIndex;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.metadata = new HashMap<>();
    }
    
    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    
    public String getSourceFile() { return sourceFile; }
    public void setSourceFile(String sourceFile) { this.sourceFile = sourceFile; }
    
    public String getDocumentTitle() { return documentTitle; }
    public void setDocumentTitle(String documentTitle) { this.documentTitle = documentTitle; }
    
    public int getChunkIndex() { return chunkIndex; }
    public void setChunkIndex(int chunkIndex) { this.chunkIndex = chunkIndex; }
    
    public int getStartPosition() { return startPosition; }
    public void setStartPosition(int startPosition) { this.startPosition = startPosition; }
    
    public int getEndPosition() { return endPosition; }
    public void setEndPosition(int endPosition) { this.endPosition = endPosition; }
    
    public Map<String, String> getMetadata() { return metadata; }
    public void setMetadata(Map<String, String> metadata) { this.metadata = metadata; }
    
    /**
     * Add metadata
     */
    public void addMetadata(String key, String value) {
        this.metadata.put(key, value);
    }
    
    /**
     * Get word count
     */
    public int getWordCount() {
        return text.split("\\s+").length;
    }
    
    /**
     * Get character count
     */
    public int getCharCount() {
        return text.length();
    }
    
    /**
     * Check if chunk is valid (has content)
     */
    public boolean isValid() {
        return text != null && !text.trim().isEmpty();
    }
    
    @Override
    public String toString() {
        return String.format("Chunk{id='%s', source='%s', index=%d, words=%d}", 
            id, sourceFile, chunkIndex, getWordCount());
    }
    
    /**
     * Get preview of text (first N characters)
     */
    public String getPreview(int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }
}