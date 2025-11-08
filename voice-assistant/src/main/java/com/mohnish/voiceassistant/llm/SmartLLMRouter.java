package com.mohnish.voiceassistant.llm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class SmartLLMRouter {
    private static final Logger logger = LoggerFactory.getLogger(SmartLLMRouter.class);
    
    private GroqClient groq;
    private OllamaClient ollama;
    private boolean preferLocal = false;
    private int requestCount = 0;
    private int groqSuccessCount = 0;
    private int ollamaFallbackCount = 0;
    
    public SmartLLMRouter(String groqApiKey, String ollamaUrl) {
        this.groq = new GroqClient(groqApiKey);
        this.ollama = new OllamaClient(ollamaUrl);
        logger.info("Smart LLM Router initialized");
    }
    
    /**
     * Generate response with automatic fallback
     */
    public String generate(String question) {
        requestCount++;
        logger.info("Processing request #{}", requestCount);
        
        // Try Groq first (unless local preferred)
        if (!preferLocal && isOnline()) {
            try {
                logger.info("🚀 Using Groq (cloud)...");
                long startTime = System.currentTimeMillis();
                String response = groq.chat(question);
                long duration = System.currentTimeMillis() - startTime;
                groqSuccessCount++;
                logger.info("✅ Groq response received in {}ms", duration);
                return response;
            } catch (Exception e) {
                logger.warn("⚠️  Groq failed: {}", e.getMessage());
                logger.info("Falling back to local Ollama...");
            }
        }
        
        // Fallback to Ollama
        try {
            logger.info("🏠 Using Ollama (local)...");
            long startTime = System.currentTimeMillis();
            String response = ollama.chat(question);
            long duration = System.currentTimeMillis() - startTime;
            ollamaFallbackCount++;
            logger.info("✅ Ollama response received in {}ms", duration);
            return response;
        } catch (Exception e) {
            logger.error("❌ Both Groq and Ollama failed", e);
            return "I'm sorry, I'm having trouble processing your request right now. Please try again.";
        }
    }
    
    /**
     * Check if internet is available
     */
    private boolean isOnline() {
        try {
            HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("https://api.groq.com"))
                .timeout(Duration.ofSeconds(2))
                .GET()
                .build();
            
            HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
            );
            
            return response.statusCode() < 500;
        } catch (Exception e) {
            logger.debug("Offline detection: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Test both connections
     */
    public void testConnections() {
        System.out.println("\n═══════════════════════════════════════");
        System.out.println("    Testing LLM Connections");
        System.out.println("═══════════════════════════════════════");
        
        // Test Groq
        System.out.print("Testing Groq API... ");
        if (groq.testConnection()) {
            System.out.println("✅ Connected");
        } else {
            System.out.println("❌ Failed");
        }
        
        // Test Ollama
        System.out.print("Testing Ollama... ");
        if (ollama.testConnection()) {
            System.out.println("✅ Connected");
        } else {
            System.out.println("❌ Failed (is 'ollama serve' running?)");
        }
        
        System.out.println("═══════════════════════════════════════\n");
    }
    
    /**
     * Get statistics
     */
    public String getStats() {
        return String.format(
            "Total requests: %d | Groq: %d | Ollama: %d",
            requestCount, groqSuccessCount, ollamaFallbackCount
        );
    }
    
    // Getters and setters
    public void setPreferLocal(boolean preferLocal) {
        this.preferLocal = preferLocal;
        logger.info("Prefer local mode: {}", preferLocal);
    }
    
    public boolean isPreferLocal() {
        return preferLocal;
    }
    
    public int getRequestCount() {
        return requestCount;
    }
}