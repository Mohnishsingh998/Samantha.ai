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
        
        // SMART PATH FINDING - try multiple locations
        String[] possiblePaths = {
            "books",
            "./books",
            "../books",
            "../../books",
            System.getProperty("user.dir") + "/books",
            System.getProperty("user.home") + "/Projects/voice-assistant/voice-assistant/books"
        };
        
        String booksDirectory = null;
        File booksDir = null;
        
        System.out.println("🔍 Searching for books directory...");
        System.out.println("Current working directory: " + System.getProperty("user.dir"));
        System.out.println();
        
        for (String path : possiblePaths) {
            File dir = new File(path);
            if (dir.exists() && dir.isDirectory()) {
                // Check if it actually has PDF files
                File[] testPdfs = dir.listFiles((d, name) -> name.endsWith(".pdf"));
                if (testPdfs != null && testPdfs.length > 0) {
                    booksDirectory = path;
                    booksDir = dir;
                    System.out.println("✅ Found books directory: " + dir.getAbsolutePath());
                    break;
                }
            }
        }
        
        if (booksDirectory == null) {
            System.err.println("❌ Books directory not found or no PDF files!");
            System.err.println("\nSearched in:");
            for (String path : possiblePaths) {
                System.err.println("  - " + new File(path).getAbsolutePath());
            }
            System.err.println("\nPlease:");
            System.err.println("  1. Create 'books/' folder in your project root");
            System.err.println("  2. Add PDF files to it");
            System.err.println("  3. Make sure you're running from the correct directory");
            System.exit(1);
        }
        
        try {
            // List PDF files
            File[] pdfFiles = booksDir.listFiles((dir, name) -> name.endsWith(".pdf"));
            
            // Filter out empty files
            int validCount = 0;
            System.out.println("Found " + pdfFiles.length + " PDF file(s):");
            for (int i = 0; i < pdfFiles.length; i++) {
                long sizeInBytes = pdfFiles[i].length();
                long sizeInKB = sizeInBytes / 1024;
                long sizeInMB = sizeInBytes / (1024 * 1024);
                
                if (sizeInBytes > 0) {
                    validCount++;
                    String sizeStr = sizeInMB > 0 ? sizeInMB + " MB" : sizeInKB + " KB";
                    System.out.println("  " + (i + 1) + ". " + pdfFiles[i].getName() + 
                        " (" + sizeStr + ")");
                } else {
                    System.out.println("  " + (i + 1) + ". " + pdfFiles[i].getName() + 
                        " (⚠️  empty - will skip)");
                }
            }
            
            if (validCount == 0) {
                System.err.println("\n❌ No valid PDF files found (all are empty)!");
                System.exit(1);
            }
            
            System.out.println();
            
            // Initialize indexer
            System.out.println("Initializing Knowledge Base Indexer...");
            System.out.println("  ChromaDB: " + chromaUrl);
            System.out.println("  Ollama: " + ollamaUrl);
            System.out.println("  Collection: " + collectionName);
            
            KnowledgeBaseIndexer indexer = new KnowledgeBaseIndexer(
                chromaUrl, ollamaUrl, collectionName
            );
            
            // Initialize collection
            System.out.println("\nCreating/connecting to collection...");
            indexer.initializeCollection();
            System.out.println("✅ Collection ready\n");
            
            // Ask user which books to index
            Scanner scanner = new Scanner(System.in);
            System.out.println("═".repeat(50));
            System.out.print("Index all books? (y/n): ");
            String response = scanner.nextLine().trim().toLowerCase();
            
            if (response.equals("y") || response.equals("yes")) {
                // Index all books
                System.out.println("\n" + "═".repeat(50));
                System.out.println("  INDEXING ALL BOOKS");
                System.out.println("═".repeat(50));
                System.out.println("⚠️  This may take several minutes depending on book size");
                System.out.println("📊 Progress will be shown for each book\n");
                
                long overallStart = System.currentTimeMillis();
                List<IndexingResult> results = indexer.indexDirectory(booksDirectory);
                long overallDuration = System.currentTimeMillis() - overallStart;
                
                // Display results
                System.out.println("\n" + "═".repeat(50));
                System.out.println("  INDEXING RESULTS");
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
                
                long avgTimePerBook = results.isEmpty() ? 0 : overallDuration / results.size();
                
                System.out.println("\n" + "═".repeat(50));
                System.out.println("  SUMMARY");
                System.out.println("═".repeat(50));
                System.out.println("📚 Books processed: " + results.size());
                System.out.println("✅ Successfully indexed: " + successCount);
                System.out.println("❌ Failed: " + (results.size() - successCount));
                System.out.println("📦 Total chunks stored: " + totalChunks);
                System.out.println("⏱️  Total time: " + String.format("%.2f", overallDuration / 1000.0) + " seconds");
                System.out.println("⏱️  Average per book: " + String.format("%.2f", avgTimePerBook / 1000.0) + " seconds");
                System.out.println("═".repeat(50));
                
            } else {
                // Index single book
                System.out.print("\nEnter book number to index (1-" + pdfFiles.length + "): ");
                int bookIndex;
                try {
                    bookIndex = Integer.parseInt(scanner.nextLine().trim()) - 1;
                } catch (NumberFormatException e) {
                    System.err.println("❌ Invalid number!");
                    System.exit(1);
                    return;
                }
                
                if (bookIndex < 0 || bookIndex >= pdfFiles.length) {
                    System.err.println("❌ Invalid book number! Must be between 1 and " + pdfFiles.length);
                    System.exit(1);
                }
                
                File selectedBook = pdfFiles[bookIndex];
                
                // Check if file is empty
                if (selectedBook.length() == 0) {
                    System.err.println("❌ Selected file is empty! Choose a different book.");
                    System.exit(1);
                }
                
                System.out.println("\n" + "═".repeat(50));
                System.out.println("  INDEXING: " + selectedBook.getName());
                System.out.println("═".repeat(50));
                System.out.println("📊 This may take a few minutes...\n");
                
                long startTime = System.currentTimeMillis();
                IndexingResult result = indexer.indexBook(selectedBook);
                long duration = System.currentTimeMillis() - startTime;
                
                System.out.println("\n" + "═".repeat(50));
                System.out.println("  RESULT");
                System.out.println("═".repeat(50));
                System.out.println(result);
                
                if (result.isSuccess()) {
                    System.out.println("\n📊 Details:");
                    System.out.println("  Characters extracted: " + result.getCharactersExtracted());
                    System.out.println("  Chunks created: " + result.getChunksCreated());
                    System.out.println("  Embeddings generated: " + result.getEmbeddingsGenerated());
                    System.out.println("  Time taken: " + String.format("%.2f", duration / 1000.0) + " seconds");
                }
                
                System.out.println("═".repeat(50));
            }
            
            System.out.println("\n🎉 Indexing complete!");
            System.out.println("📚 Your books are now searchable in the knowledge base!");
            System.out.println("\n💡 Next step: Run RetrievalTest to search your books!");
            
        } catch (Exception e) {
            System.err.println("\n❌ Error: " + e.getMessage());
            System.err.println("\n📋 Stack trace:");
            e.printStackTrace();
            
            System.err.println("\n💡 Troubleshooting:");
            System.err.println("  1. Make sure ChromaDB is running: chromadb run --path ./chroma_data --port 8000");
            System.err.println("  2. Make sure Ollama is running: ollama serve");
            System.err.println("  3. Check that your PDF is valid and not corrupted");
        }
    }
}