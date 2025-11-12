package com.mohnish.voiceassistant.document;

import java.io.File;
import java.util.List;

public class TextChunkerTest {
    public static void main(String[] args) {
        System.out.println("✂️  Text Chunker Test\n");
        
        DocumentParser parser = new DocumentParser();
        File booksDir = new File("books");
        
        if (!booksDir.exists() || !booksDir.isDirectory()) {
            System.err.println("❌ 'books' directory not found!");
            System.exit(1);
        }
        
        File[] files = booksDir.listFiles((dir, name) -> 
            name.toLowerCase().endsWith(".pdf") || name.toLowerCase().endsWith(".txt")
        );
        
        if (files == null || files.length == 0) {
            System.err.println("❌ No documents found!");
            System.exit(1);
        }
        
        // Test different strategies
        TextChunker.ChunkingStrategy[] strategies = {
            TextChunker.ChunkingStrategy.SENTENCE_BOUNDARY,
            TextChunker.ChunkingStrategy.FIXED_SIZE,
            TextChunker.ChunkingStrategy.PARAGRAPH_BOUNDARY
        };
        
        // Process first file only for demo
        File testFile = files[0];
        System.out.println("Testing with: " + testFile.getName());
        System.out.println("═".repeat(60));
        
        try {
            // Extract text
            String text = parser.extractText(testFile);
            DocumentMetadata metadata = parser.getMetadata(testFile);
            
            System.out.println("\n📄 Document Info:");
            System.out.println("  Title: " + metadata.getTitle());
            System.out.println("  Words: " + text.split("\\s+").length);
            System.out.println("  Characters: " + text.length());
            
            // Test each strategy
            for (TextChunker.ChunkingStrategy strategy : strategies) {
                System.out.println("\n" + "─".repeat(60));
                System.out.println("Strategy: " + strategy);
                System.out.println("─".repeat(60));
                
                TextChunker chunker = new TextChunker(500, 50, strategy);
                
                long startTime = System.currentTimeMillis();
                List<DocumentChunk> chunks = chunker.chunkDocument(
                    text, 
                    testFile.getName(),
                    metadata.getTitle()
                );
                long duration = System.currentTimeMillis() - startTime;
                
                System.out.println("\n📊 Results:");
                System.out.println("  Total chunks: " + chunks.size());
                System.out.println("  Processing time: " + duration + "ms");
                
                if (!chunks.isEmpty()) {
                    // Statistics
                    int minWords = Integer.MAX_VALUE;
                    int maxWords = 0;
                    int totalWords = 0;
                    
                    for (DocumentChunk chunk : chunks) {
                        int words = chunk.getWordCount();
                        minWords = Math.min(minWords, words);
                        maxWords = Math.max(maxWords, words);
                        totalWords += words;
                    }
                    
                    int avgWords = totalWords / chunks.size();
                    
                    System.out.println("  Words per chunk:");
                    System.out.println("    Min: " + minWords);
                    System.out.println("    Max: " + maxWords);
                    System.out.println("    Avg: " + avgWords);
                    
                    // Show first chunk as example
                    System.out.println("\n📖 Example (Chunk #1):");
                    DocumentChunk firstChunk = chunks.get(0);
                    System.out.println("  ID: " + firstChunk.getId());
                    System.out.println("  Words: " + firstChunk.getWordCount());
                    System.out.println("  Preview:");
                    System.out.println("  " + "┌" + "─".repeat(58) + "┐");
                    String preview = firstChunk.getPreview(200);
                    for (String line : wrapText(preview, 56)) {
                        System.out.println("  │ " + line + " ".repeat(56 - line.length()) + " │");
                    }
                    System.out.println("  └" + "─".repeat(58) + "┘");
                }
            }
            
            System.out.println("\n" + "═".repeat(60));
            System.out.println("✅ Chunking test complete!");
            System.out.println("═".repeat(60));
            
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Wrap text to fit in specified width
     */
    private static List<String> wrapText(String text, int width) {
        List<String> lines = new java.util.ArrayList<>();
        String[] words = text.split("\\s+");
        StringBuilder currentLine = new StringBuilder();
        
        for (String word : words) {
            if (currentLine.length() + word.length() + 1 <= width) {
                if (currentLine.length() > 0) currentLine.append(" ");
                currentLine.append(word);
            } else {
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                }
                currentLine = new StringBuilder(word);
            }
        }
        
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }
        
        return lines;
    }
}