package com.mohnish.voiceassistant.embedding;

import com.mohnish.voiceassistant.document.DocumentParser;
import com.mohnish.voiceassistant.document.TextChunker;
import com.mohnish.voiceassistant.document.DocumentChunk;

import java.io.File;
import java.util.List;

public class RealBookEmbeddingTest {
    public static void main(String[] args) {
        System.out.println("📚 Real Book Embedding Test\n");
        
        // Check for books
        File booksDir = new File("books");
        if (!booksDir.exists() || booksDir.listFiles() == null) {
            System.err.println("❌ No books/ directory found!");
            System.err.println("Please create books/ and add a PDF file.");
            System.exit(1);
        }
        
        File[] pdfFiles = booksDir.listFiles((dir, name) -> name.endsWith(".pdf"));
        if (pdfFiles == null || pdfFiles.length == 0) {
            System.err.println("❌ No PDF files found in books/ directory!");
            System.exit(1);
        }
        
        try {
            // Use first PDF
            File bookFile = pdfFiles[0];
            System.out.println("Using book: " + bookFile.getName());
            System.out.println();
            
            // Step 1: Extract text
            System.out.println("Step 1: Extracting text from PDF...");
            DocumentParser parser = new DocumentParser();
            String text = parser.extractText(bookFile);
            System.out.println("✅ Extracted " + text.length() + " characters\n");
            
            // Step 2: Chunk text
            System.out.println("Step 2: Chunking text...");
            TextChunker chunker = new TextChunker();

            // text = extracted PDF text
            String source = bookFile.getName();
            String title = bookFile.getName(); // or metadata title

            List<DocumentChunk> chunks = chunker.chunkDocument(text, source, title);
            System.out.println("✅ Created " + chunks.size() + " chunks\n");
            
            // Step 3: Generate embeddings for first 3 chunks
            System.out.println("Step 3: Generating embeddings for first 3 chunks...");
            EmbeddingGenerator generator = new EmbeddingGenerator("http://localhost:11434");
            
            List<DocumentChunk> sampleChunks = chunks.subList(0, Math.min(3, chunks.size()));
            
            for (int i = 0; i < sampleChunks.size(); i++) {
                DocumentChunk chunk = sampleChunks.get(i);
                System.out.println("\nChunk " + (i + 1) + ":");
                System.out.println("  ID: " + chunk.getId());
                System.out.println("  Text preview: " + chunk.getText().substring(0, Math.min(100, chunk.getText().length())) + "...");
                
                long startTime = System.currentTimeMillis();
                List<Double> embedding = generator.generateForChunk(chunk);
                long duration = System.currentTimeMillis() - startTime;
                
                System.out.println("  ✅ Embedding generated:");
                System.out.println("     Dimensions: " + embedding.size());
                System.out.println("     Time: " + duration + "ms");
                System.out.println("     Sample values: " + embedding.subList(0, 3));
            }
            
            System.out.println("\n" + "═".repeat(50));
            System.out.println("🎉 Successfully generated embeddings from real book!");
            System.out.println("═".repeat(50));
            System.out.println("\nYou're ready to index entire books!");
            
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}