package com.mohnish.voiceassistant.indexing;

public class IndexingResult {
    private String bookName;
    private boolean success;
    private int charactersExtracted;
    private int chunksCreated;
    private int embeddingsGenerated;
    private int chunksStored;
    private long durationMs;
    private String errorMessage;
    
    public IndexingResult(String bookName) {
        this.bookName = bookName;
        this.success = false;
    }
    
    // Getters and setters
    public String getBookName() { return bookName; }
    public void setBookName(String bookName) { this.bookName = bookName; }
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public int getCharactersExtracted() { return charactersExtracted; }
    public void setCharactersExtracted(int charactersExtracted) { 
        this.charactersExtracted = charactersExtracted; 
    }
    
    public int getChunksCreated() { return chunksCreated; }
    public void setChunksCreated(int chunksCreated) { 
        this.chunksCreated = chunksCreated; 
    }
    
    public int getEmbeddingsGenerated() { return embeddingsGenerated; }
    public void setEmbeddingsGenerated(int embeddingsGenerated) { 
        this.embeddingsGenerated = embeddingsGenerated; 
    }
    
    public int getChunksStored() { return chunksStored; }
    public void setChunksStored(int chunksStored) { 
        this.chunksStored = chunksStored; 
    }
    
    public long getDurationMs() { return durationMs; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { 
        this.errorMessage = errorMessage; 
    }
    
    @Override
    public String toString() {
        if (success) {
            return String.format(
                "✅ %s: %d chars → %d chunks → %d embeddings (%d ms)",
                bookName, charactersExtracted, chunksCreated, 
                embeddingsGenerated, durationMs
            );
        } else {
            return String.format(
                "❌ %s: Failed - %s",
                bookName, errorMessage
            );
        }
    }
}