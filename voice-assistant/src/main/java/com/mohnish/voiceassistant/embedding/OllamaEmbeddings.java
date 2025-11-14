package com.mohnish.voiceassistant.embedding;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class OllamaEmbeddings {
    private static final Logger logger = LoggerFactory.getLogger(OllamaEmbeddings.class);
    
    private final String baseUrl;
    private final HttpClient httpClient;
    private final Gson gson;
    private final String model = "nomic-embed-text";
    
    public OllamaEmbeddings(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
        this.gson = new Gson();
        logger.info("Ollama Embeddings initialized with model: {}", model);
    }
    
    /**
     * Generate embedding for a single text
     */
    public List<Double> generateEmbedding(String text) throws Exception {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Text cannot be empty");
        }
        
        logger.debug("Generating embedding for text ({} chars)", text.length());
        
        // Build request body
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", model);
        requestBody.addProperty("prompt", text);
        
        // Create HTTP request
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/api/embeddings"))
            .header("Content-Type", "application/json")
            .timeout(Duration.ofSeconds(30))
            .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
            .build();
        
        // Send request
        HttpResponse<String> response = httpClient.send(
            request,
            HttpResponse.BodyHandlers.ofString()
        );
        
        // Check response
        if (response.statusCode() != 200) {
            throw new Exception("Embedding API error: " + response.statusCode());
        }
        
        // Parse response
        JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);
        
        // Extract embedding array
        List<Double> embedding = new ArrayList<>();
        jsonResponse.getAsJsonArray("embedding").forEach(element -> {
            embedding.add(element.getAsDouble());
        });
        
        logger.debug("Generated embedding with {} dimensions", embedding.size());
        return embedding;
    }
    
    /**
     * Generate embeddings for multiple texts (batch processing)
     */
    public List<List<Double>> generateEmbeddings(List<String> texts) throws Exception {
        logger.info("Generating embeddings for {} texts", texts.size());
        
        List<List<Double>> embeddings = new ArrayList<>();
        int count = 0;
        
        for (String text : texts) {
            count++;
            logger.info("Processing text {}/{}", count, texts.size());
            
            try {
                List<Double> embedding = generateEmbedding(text);
                embeddings.add(embedding);
                
                // Small delay to avoid overwhelming Ollama
                if (count < texts.size()) {
                    Thread.sleep(100);
                }
                
            } catch (Exception e) {
                logger.error("Failed to generate embedding for text {}: {}", count, e.getMessage());
                // Add empty embedding or rethrow based on your needs
                throw e;
            }
        }
        
        logger.info("✅ Generated {} embeddings successfully", embeddings.size());
        return embeddings;
    }
    
    /**
     * Get embedding dimension size
     */
    public int getDimensionSize() throws Exception {
        // Generate a test embedding to determine dimension
        List<Double> testEmbedding = generateEmbedding("test");
        return testEmbedding.size();
    }
    
    /**
     * Test connection to Ollama
     */
    public boolean testConnection() {
        try {
            generateEmbedding("test");
            return true;
        } catch (Exception e) {
            logger.error("Connection test failed", e);
            return false;
        }
    }
}