package com.mohnish.voiceassistant.llm;

import java.util.Scanner;

public class LLMTest {
    public static void main(String[] args) {
        System.out.println("🤖 LLM Integration Test\n");
        
        // Get API key from environment
        String groqApiKey = System.getenv("GROQ_API_KEY");
        if (groqApiKey == null || groqApiKey.trim().isEmpty()) {
            System.err.println("❌ Error: GROQ_API_KEY not set!");
            System.err.println("\nPlease run:");
            System.err.println("  export GROQ_API_KEY='your-key'");
            System.err.println("\nGet your API key from: https://console.groq.com");
            System.exit(1);
        }
        
        SmartLLMRouter llm = new SmartLLMRouter(groqApiKey, "http://localhost:11434");
        
        // Test connections
        llm.testConnections();
        
        // Test questions
        String[] testQuestions = {
            "What is artificial intelligence?",
            "Explain machine learning briefly.",
            "What are neural networks?",
            "What is photosynthesis?",
            "Who was Albert Einstein?"
        };
        
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║      Running Test Questions            ║");
        System.out.println("╚════════════════════════════════════════╝\n");
        
        for (int i = 0; i < testQuestions.length; i++) {
            System.out.println("─".repeat(50));
            System.out.println("[Question " + (i + 1) + "] " + testQuestions[i]);
            System.out.println();
            
            try {
                long startTime = System.currentTimeMillis();
                String answer = llm.generate(testQuestions[i]);
                long duration = System.currentTimeMillis() - startTime;
                
                System.out.println("[Answer] " + answer);
                System.out.println("\n⏱️  Response time: " + duration + "ms");
                System.out.println();
            } catch (Exception e) {
                System.err.println("[Error] " + e.getMessage());
                System.out.println();
            }
            
            // Small delay between requests
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
        }
        
        // Interactive mode
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║        Interactive Mode                ║");
        System.out.println("╚════════════════════════════════════════╝");
        System.out.println("Ask questions (or 'q' to quit):\n");
        
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("❓ Question: ");
            String question = scanner.nextLine();
            
            if (question.equalsIgnoreCase("q")) {
                break;
            }
            
            if (!question.trim().isEmpty()) {
                try {
                    System.out.println("🤔 Thinking...");
                    long startTime = System.currentTimeMillis();
                    String response = llm.generate(question);
                    long duration = System.currentTimeMillis() - startTime;
                    
                    System.out.println("💬 " + response);
                    System.out.println("⏱️  " + duration + "ms\n");
                } catch (Exception e) {
                    System.err.println("❌ Error: " + e.getMessage() + "\n");
                }
            }
        }
        
        System.out.println("\n📊 " + llm.getStats());
        System.out.println("✅ LLM Test completed!");
    }
}