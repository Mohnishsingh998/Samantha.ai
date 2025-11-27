package com.mohnish.voiceassistant.rag;

import java.util.Scanner;

/**
 * Interactive test for RAG Pipeline
 */
public class RAGTest {
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║      🧠 RAG PIPELINE TEST              ║");
        System.out.println("╚════════════════════════════════════════╝\n");
        
        // Configuration
        String chromaUrl = "http://localhost:8000";
        String ollamaUrl = "http://localhost:11434";
        String collectionName = "my_books";
        String groqApiKey = System.getenv("GROQ_API_KEY");
        
        // Check API key
        if (groqApiKey == null || groqApiKey.isEmpty()) {
            System.err.println("⚠️  Warning: GROQ_API_KEY not set!");
            System.err.println("Set it with: export GROQ_API_KEY='your-key'");
            System.err.println("Will use Ollama instead (slower)...\n");
        }
        
        try {
            // Initialize RAG pipeline
            System.out.println("🔧 Initializing RAG Pipeline...");
            RAGPipeline rag = new RAGPipeline(
                chromaUrl, 
                ollamaUrl, 
                collectionName, 
                groqApiKey
            );
            
            // Test connections
            System.out.println("🔌 Testing connections...");
            if (!rag.testConnection()) {
                System.err.println("❌ Connection test failed!");
                System.err.println("Make sure services are running:");
                System.err.println("  - ChromaDB: chromadb run --path ./chroma_data --port 8000");
                System.err.println("  - Ollama: ollama serve");
                return;
            }
            
            System.out.println("✅ All services connected\n");
            
            // Pre-defined test questions
            System.out.println("═══════════════════════════════════════");
            System.out.println("   AUTOMATED TESTS");
            System.out.println("═══════════════════════════════════════\n");
            
            String[] testQuestions = {
                "What is NumPy?",
                "Explain machine learning",
                "What is scikit-learn?"
            };
            
            for (int i = 0; i < testQuestions.length; i++) {
                String question = testQuestions[i];
                
                System.out.println("─".repeat(60));
                System.out.println("Test " + (i + 1) + ": " + question);
                System.out.println("─".repeat(60));
                
                long startTime = System.currentTimeMillis();
                String answer = rag.answer(question);
                long duration = System.currentTimeMillis() - startTime;
                
                System.out.println("\n💬 Answer:");
                System.out.println(wrapText(answer, 70));
                System.out.println("\n⏱️  Response time: " + duration + "ms\n");
                
                if (i < testQuestions.length - 1) {
                    Thread.sleep(1000); // Brief pause between questions
                }
            }
            
            // Interactive mode
            System.out.println("\n═══════════════════════════════════════");
            System.out.println("   INTERACTIVE MODE");
            System.out.println("═══════════════════════════════════════");
            System.out.println("Ask questions about your book!");
            System.out.println("Commands:");
            System.out.println("  'q' or 'quit'  - Exit");
            System.out.println("  'history'      - Show conversation");
            System.out.println("  'clear'        - Clear history");
            System.out.println("  'local'        - Switch to local LLM");
            System.out.println("  'cloud'        - Switch to cloud LLM");
            System.out.println();
            
            Scanner scanner = new Scanner(System.in);
            
            while (true) {
                System.out.print("❓ Your question: ");
                String input = scanner.nextLine().trim();
                
                if (input.isEmpty()) {
                    continue;
                }
                
                // Handle commands
                if (input.equalsIgnoreCase("q") || input.equalsIgnoreCase("quit")) {
                    break;
                }
                
                if (input.equalsIgnoreCase("history")) {
                    showHistory(rag);
                    continue;
                }
                
                if (input.equalsIgnoreCase("clear")) {
                    rag.clearHistory();
                    System.out.println("✅ Conversation history cleared\n");
                    continue;
                }
                
                if (input.equalsIgnoreCase("local")) {
                    rag.setPreferLocal(true);
                    System.out.println("✅ Switched to local LLM (Ollama)\n");
                    continue;
                }
                
                if (input.equalsIgnoreCase("cloud")) {
                    rag.setPreferLocal(false);
                    System.out.println("✅ Switched to cloud LLM (Groq)\n");
                    continue;
                }
                
                // Process question
                System.out.println();
                long startTime = System.currentTimeMillis();
                String answer = rag.answer(input);
                long duration = System.currentTimeMillis() - startTime;
                
                System.out.println("─".repeat(60));
                System.out.println("💬 Answer:");
                System.out.println(wrapText(answer, 70));
                System.out.println("\n⏱️  Response time: " + duration + "ms");
                System.out.println("─".repeat(60));
                System.out.println();
            }
            
            System.out.println("\n✅ RAG Pipeline test complete!");
            System.out.println("🎉 Your AI assistant is working!\n");
            
        } catch (Exception e) {
            System.err.println("\n❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Show conversation history
     */
    private static void showHistory(RAGPipeline rag) {
        var history = rag.getHistory();
        
        if (history.isEmpty()) {
            System.out.println("📜 No conversation history yet\n");
            return;
        }
        
        System.out.println("\n📜 Conversation History:");
        System.out.println("─".repeat(60));
        
        for (int i = 0; i < history.size(); i++) {
            var turn = history.get(i);
            System.out.println("\n[" + (i + 1) + "] Q: " + turn.question);
            System.out.println("    A: " + turn.answer.substring(0, 
                Math.min(100, turn.answer.length())) + "...");
        }
        
        System.out.println("─".repeat(60));
        System.out.println();
    }
    
    /**
     * Wrap text to specified width
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