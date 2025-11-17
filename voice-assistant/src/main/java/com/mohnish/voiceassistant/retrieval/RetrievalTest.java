package com.mohnish.voiceassistant.retrieval;

import com.mohnish.voiceassistant.vectordb.VectorStore;
import com.mohnish.voiceassistant.vectordb.VectorStore.SearchResult;

import java.util.List;
import java.util.Scanner;

public class RetrievalTest {
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║   🔍 KNOWLEDGE BASE RETRIEVAL TEST    ║");
        System.out.println("╚════════════════════════════════════════╝\n");
        
        // Configuration
        String chromaUrl = "http://localhost:8000";
        String ollamaUrl = "http://localhost:11434";
        String collectionName = "my_books";
        
        try {
            // Initialize VectorStore
            System.out.println("Connecting to knowledge base...");
            VectorStore vectorStore = new VectorStore(chromaUrl, ollamaUrl, collectionName);
            
            // Test connection
            if (!vectorStore.testConnection()) {
                System.err.println("❌ Cannot connect to ChromaDB!");
                System.err.println("Make sure ChromaDB is running:");
                System.err.println("  chromadb run --path ./chroma_data --port 8000");
                System.exit(1);
            }
            
            System.out.println("✅ Connected to knowledge base\n");
            
            // Pre-defined test queries
            String[] testQueries = {
                "What is machine learning?",
                "Explain artificial intelligence",
                "What are neural networks?",
                "Tell me about photosynthesis",
                "What is gravity?"
            };
            
            System.out.println("═══════════════════════════════════════");
            System.out.println("   PRE-DEFINED TEST QUERIES");
            System.out.println("═══════════════════════════════════════\n");
            
            for (int i = 0; i < testQueries.length; i++) {
                String query = testQueries[i];
                
                System.out.println("─".repeat(50));
                System.out.println("[Query " + (i + 1) + "] " + query);
                System.out.println("─".repeat(50));
                
                long startTime = System.currentTimeMillis();
                List<SearchResult> results = vectorStore.search(query, 3);
                long duration = System.currentTimeMillis() - startTime;
                
                if (results.isEmpty()) {
                    System.out.println("⚠️  No results found");
                } else {
                    System.out.println("\nTop " + results.size() + " results:\n");
                    
                    for (int j = 0; j < results.size(); j++) {
                        SearchResult result = results.get(j);
                        System.out.println("[" + (j + 1) + "] " + result);
                        System.out.println("    Source: " + result.getSource());
                        System.out.println("    Chunk: " + result.getChunkIndex());
                        System.out.println("    Relevance: " + 
                            String.format("%.1f%%", result.getRelevanceScore() * 100));
                        System.out.println("    Preview: " + result.getPreview(150));
                        System.out.println();
                    }
                }
                
                System.out.println("⏱️  Search time: " + duration + "ms\n");
                
                // Small delay between queries
                if (i < testQueries.length - 1) {
                    Thread.sleep(500);
                }
            }
            
            // Interactive mode
            System.out.println("\n═══════════════════════════════════════");
            System.out.println("   INTERACTIVE SEARCH MODE");
            System.out.println("═══════════════════════════════════════");
            System.out.println("Ask questions (or 'q' to quit):\n");
            
            Scanner scanner = new Scanner(System.in);
            
            while (true) {
                System.out.print("❓ Your question: ");
                String query = scanner.nextLine().trim();
                
                if (query.equalsIgnoreCase("q") || query.equalsIgnoreCase("quit")) {
                    break;
                }
                
                if (query.isEmpty()) {
                    continue;
                }
                
                System.out.println("\n🔍 Searching knowledge base...");
                
                long startTime = System.currentTimeMillis();
                List<SearchResult> results = vectorStore.search(query, 5);
                long duration = System.currentTimeMillis() - startTime;
                
                System.out.println("─".repeat(50));
                
                if (results.isEmpty()) {
                    System.out.println("⚠️  No relevant information found in your books.");
                    System.out.println("Try rephrasing your question or index more books.");
                } else {
                    System.out.println("Found " + results.size() + " relevant passages:\n");
                    
                    for (int i = 0; i < results.size(); i++) {
                        SearchResult result = results.get(i);
                        
                        System.out.println("━".repeat(50));
                        System.out.println("Result #" + (i + 1));
                        System.out.println("━".repeat(50));
                        System.out.println("📚 Source: " + result.getSource());
                        System.out.println("📊 Relevance: " + 
                            String.format("%.1f%%", result.getRelevanceScore() * 100));
                        System.out.println("📄 Chunk: " + result.getChunkIndex());
                        System.out.println("\n📝 Content:");
                        System.out.println(wrapText(result.getText(), 70));
                        System.out.println();
                    }
                }
                
                System.out.println("─".repeat(50));
                System.out.println("⏱️  Search completed in " + duration + "ms");
                System.out.println();
            }
            
            System.out.println("\n✅ Retrieval test complete!");
            System.out.println("Your knowledge base is searchable! 🎉");
            
        } catch (Exception e) {
            System.err.println("\n❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Wrap text to specified width for better display
     */
    private static String wrapText(String text, int width) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        
        StringBuilder wrapped = new StringBuilder();
        String[] words = text.split("\\s+");
        int lineLength = 0;
        
        for (String word : words) {
            if (lineLength + word.length() + 1 > width) {
                wrapped.append("\n");
                lineLength = 0;
            }
            
            if (lineLength > 0) {
                wrapped.append(" ");
                lineLength++;
            }
            
            wrapped.append(word);
            lineLength += word.length();
        }
        
        return wrapped.toString();
    }
}