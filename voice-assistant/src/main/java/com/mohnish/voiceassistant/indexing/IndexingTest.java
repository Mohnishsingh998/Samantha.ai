package com.mohnish.voiceassistant.indexing;

import java.io.File;
import java.util.List;
import java.util.Scanner;

public class IndexingTest {
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║   📚 KNOWLEDGE BASE INDEXING TEST     ║");
        System.out.println("╚════════════════════════════════════════╝\n");
        
        // Configuration
        String chromaUrl = "http://localhost:8000";
        String ollamaUrl = "http://localhost:11434";
        String collectionName = "my_books";
        String booksDirectory = "books";
        
        try {
            // Check if books directory exists
            File booksDir = new File(booksDirectory);
            if (!booksDir.exists()) {
                System.err.println("❌ Books directory not found!");
                System.err.println("Please create 'books/' folder and add PDF files.");
                System.exit(1);
            }
            
            File[] pdfFiles = booksDir.listFiles((dir, name) -> name.endsWith(".pdf"));
            if (pdfFiles == null || pdfFiles.length == 0) {
                System.err.println("❌ No PDF files found in books/ directory!");
                System.exit(1);
            }
            
            System.out.println("Found " + pdfFiles.length + " PDF file(s):");
            for (int i = 0; i < pdfFiles.length; i++) {
                System.out.println("  " + (i + 1) + ". " + pdfFiles[i].getName());
            }
            System.out.println();
            
            // Initialize indexer
            System.out.println("Initializing Knowledge Base Indexer...");
            KnowledgeBaseIndexer indexer = new KnowledgeBaseIndexer(
                chromaUrl, ollamaUrl, collectionName
            );
            
            // Initialize collection
            System.out.println("Creating collection: " + collectionName);
            indexer.initializeCollection();
            System.out.println("✅ Collection ready\n");
            
            // Ask user which books to index
            Scanner scanner = new Scanner(System.in);
            System.out.print("Index all books? (y/n): ");
            String response = scanner.nextLine().trim().toLowerCase();
            
            if (response.equals("y") || response.equals("yes")) {
                // Index all books
                System.out.println("\n" + "═".repeat(50));
                System.out.println("Starting indexing of all books...");
                System.out.println("═".repeat(50) + "\n");
                
                long overallStart = System.currentTimeMillis();
                List<IndexingResult> results = indexer.indexDirectory(booksDirectory);
                long overallDuration = System.currentTimeMillis() - overallStart;
                
                // Display results
                System.out.println("\n" + "═".repeat(50));
                System.out.println("INDEXING RESULTS");
                System.out.println("═".repeat(50));
                
                for (IndexingResult result : results) {
                    System.out.println(result);
                }
                
                // Summary
                long successCount = results.stream()
                    .filter(IndexingResult::isSuccess)
                    .count();
                
                int totalChunks = results.stream()
                    .mapToInt(IndexingResult::getChunksStored)
                    .sum();
                
                System.out.println("\n" + "═".repeat(50));
                System.out.println("SUMMARY");
                System.out.println("═".repeat(50));
                System.out.println("Books processed: " + results.size());
                System.out.println("Successfully indexed: " + successCount);
                System.out.println("Total chunks stored: " + totalChunks);
                System.out.println("Total time: " + (overallDuration / 1000.0) + " seconds");
                System.out.println("═".repeat(50));
                
            } else {
                // Index single book
                System.out.print("Enter book number to index (1-" + pdfFiles.length + "): ");
                int bookIndex = Integer.parseInt(scanner.nextLine()) - 1;
                
                if (bookIndex < 0 || bookIndex >= pdfFiles.length) {
                    System.err.println("Invalid book number!");
                    System.exit(1);
                }
                
                File selectedBook = pdfFiles[bookIndex];
                System.out.println("\nIndexing: " + selectedBook.getName());
                System.out.println("═".repeat(50) + "\n");
                
                IndexingResult result = indexer.indexBook(selectedBook);
                
                System.out.println("\n" + "═".repeat(50));
                System.out.println("RESULT");
                System.out.println("═".repeat(50));
                System.out.println(result);
                System.out.println("═".repeat(50));
            }
            
            System.out.println("\n🎉 Indexing complete!");
            System.out.println("Your books are now searchable in the knowledge base!");
            
        } catch (Exception e) {
            System.err.println("\n❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}