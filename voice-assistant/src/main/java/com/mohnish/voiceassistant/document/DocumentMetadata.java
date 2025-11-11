package com.mohnish.voiceassistant.document;

/**
 * Metadata about a document
 */
public class DocumentMetadata {
    private String filename;
    private String filePath;
    private long fileSize;
    private int pageCount;
    private String title;
    private String author;
    private String subject;
    
    // Getters and setters
    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
    
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    
    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
    
    public int getPageCount() { return pageCount; }
    public void setPageCount(int pageCount) { this.pageCount = pageCount; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    
    @Override
    public String toString() {
        return String.format("Document{name='%s', title='%s', pages=%d, size=%d bytes}", 
            filename, title, pageCount, fileSize);
    }
    
    /**
     * Get human-readable file size
     */
    public String getFileSizeFormatted() {
        long kb = fileSize / 1024;
        if (kb < 1024) {
            return kb + " KB";
        }
        long mb = kb / 1024;
        return mb + " MB";
    }
}