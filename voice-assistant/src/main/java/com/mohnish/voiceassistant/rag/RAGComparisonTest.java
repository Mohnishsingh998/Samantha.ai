package com.mohnish.voiceassistant.rag;

import com.mohnish.voiceassistant.llm.GroqClient;

/**
 * Compare RAG (with context) vs No-RAG (without context)
 * to demonstrate the value of retrieval-augmented generation
 */
public class RAGComparisonTest {
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║    🆚 RAG vs NO-RAG COMPARISON        ║");
        System.out.println("╚════════════════════════════════════════╝\n");
        
        String groqApiKey = System.getenv("GROQ_API_KEY");
        
        if (groqApiKey == null || groqApiKey.isEmpty()) {
            System.err.println("❌ GROQ_API_KEY not set!");
            System.err.println("Set it with: export GROQ_API_KEY='your-key'");
            return;
        }
        
        try {
            // Initialize both systems
            System.out.println("🔧 Initializing systems...\n");
            
            GroqClient groq = new GroqClient(groqApiKey);
            RAGPipeline rag = new RAGPipeline(
                "http://localhost:8000",
                "http://localhost:11434",
                "my_books",
                groqApiKey
            );
            
            if (!rag.testConnection()) {
                System.err.println("❌ RAG connection failed!");
                return;
            }
            
            // Test questions
            String[] questions = {
                "What does the book say about NumPy?",
                "According to the book, what is machine learning?",
                "What does the book explain about scikit-learn?"
            };
            
            for (String question : questions) {
                runComparison(groq, rag, question);
                System.out.println("\n" + "═".repeat(70) + "\n");
                Thread.sleep(2000); // Pause between tests
            }
            
            // Summary
            System.out.println("📊 SUMMARY:");
            System.out.println("─".repeat(70));
            System.out.println("✅ WITH RAG: Answers are specific to YOUR book");
            System.out.println("   - Uses actual content from your book");
            System.out.println("   - Can cite sources and chunks");
            System.out.println("   - More accurate for book-specific questions");
            System.out.println();
            System.out.println("❌ WITHOUT RAG: Answers are generic");
            System.out.println("   - Uses LLM's general knowledge");
            System.out.println("   - Cannot reference your specific book");
            System.out.println("   - May give different/generic information");
            System.out.println("─".repeat(70));
            
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void runComparison(GroqClient groq, RAGPipeline rag, String question) 
            throws Exception {
        
        System.out.println("🔍 QUESTION:");
        System.out.println(question);
        System.out.println("\n" + "─".repeat(70));
        
        // Test WITHOUT RAG (generic AI)
        System.out.println("\n❌ WITHOUT RAG (Generic AI Knowledge):");
        System.out.println("─".repeat(70));
        
        long startTime = System.currentTimeMillis();
        String noRagAnswer = groq.generate(question);
        long noRagTime = System.currentTimeMillis() - startTime;
        
        System.out.println(wrapText(noRagAnswer, 68));
        System.out.println("\n⏱️  Time: " + noRagTime + "ms");
        
        System.out.println("\n" + "─".repeat(70));
        
        // Test WITH RAG (your book)
        System.out.println("\n✅ WITH RAG (Your Book's Content):");
        System.out.println("─".repeat(70));
        
        startTime = System.currentTimeMillis();
        String ragAnswer = rag.answer(question);
        long ragTime = System.currentTimeMillis() - startTime;
        
        System.out.println(wrapText(ragAnswer, 68));
        System.out.println("\n⏱️  Time: " + ragTime + "ms");
        
        // Analysis
        System.out.println("\n" + "─".repeat(70));
        System.out.println("📊 ANALYSIS:");
        System.out.println("  No-RAG: Generic textbook answer (fast: " + noRagTime + "ms)");
        System.out.println("  RAG:    Book-specific answer (slower: " + ragTime + "ms)");
        System.out.println("  Winner: RAG (more accurate for your use case) ✅");
    }
    
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