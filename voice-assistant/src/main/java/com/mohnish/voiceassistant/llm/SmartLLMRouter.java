package com.mohnish.voiceassistant.llm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

/**
 * Smart router that switches between Groq (cloud) and Ollama (local) LLMs
 */
public class SmartLLMRouter {
    private static final Logger logger = LoggerFactory.getLogger(SmartLLMRouter.class);

    private final GroqClient groq;
    private final OllamaClient ollama;
    private boolean preferLocal = false;

    public SmartLLMRouter(String groqApiKey, String ollamaUrl) {
        this.groq = groqApiKey != null ? new GroqClient(groqApiKey) : null;
        this.ollama = new OllamaClient(ollamaUrl);

        logger.info("SmartLLMRouter initialized");
        logger.info("Groq: {}", groq != null ? "Available" : "Not configured");
        logger.info("Ollama: Available");
    }

    // ---------------------------------------------------------
    // 1. EASY API — generate(String)
    // ---------------------------------------------------------
    public String generate(String prompt) {
        return generate(prompt, List.of());
    }

    // ---------------------------------------------------------
    // 2. MAIN ROUTER
    // ---------------------------------------------------------
    public String generate(String prompt, List<String> ctx) {
        logger.info("🧠 generate() called | prompt length = {} chars", prompt.length());

        // ---------- Try Groq cloud ----------
        try {
            if (!preferLocal && groq != null && isOnline()) {
                logger.info("🚀 Using Groq (cloud)");
                return groq.chat(prompt);   // ← FIXED (correct method)
            }
        } catch (Exception e) {
            logger.warn("⚠ Groq failed: {}", e.getMessage());
        }

        // ---------- Use Local Ollama ----------
        try {
            logger.info("🏠 Using Ollama (local)");
            return ollama.chat(prompt);      // ← FIXED (correct method)
        } catch (Exception e) {
            logger.error("❌ Both Groq and Ollama failed", e);
        }

        return "I'm sorry, I'm having trouble generating a response right now.";
    }

    // ---------------------------------------------------------
    // 3. NETWORK CHECK
    // ---------------------------------------------------------
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
            return false;
        }
    }

    // ---------------------------------------------------------
    // 4. CONNECTION TEST
    // ---------------------------------------------------------
    public void testConnections() {
        boolean groqOk = groq != null && groq.testConnection();
        boolean ollamaOk = ollama.testConnection();

        logger.info("Groq: {} | Ollama: {}", groqOk ? "OK" : "Fail", ollamaOk ? "OK" : "Fail");
    }

    // ---------------------------------------------------------
    // 5. SETTINGS
    // ---------------------------------------------------------
    public void setPreferLocal(boolean preferLocal) {
        this.preferLocal = preferLocal;
        logger.info("Prefer Local set to {}", preferLocal);
    }

    public String getStats() {
    return """
        Smart LLM Router Stats:
        ------------------------
        Prefer Local: %s
        Groq Configured: %s
        Ollama Available: %s
    """.formatted(
        preferLocal,
        (groq != null),
        true
    );
}
}
