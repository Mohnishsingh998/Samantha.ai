package com.mohnish.voiceassistant.llm;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
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

public class GroqClient {
    private static final Logger logger = LoggerFactory.getLogger(GroqClient.class);
    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    
    private final String apiKey;
    private final HttpClient httpClient;
    private final Gson gson;
    private String model = "llama-3.3-70b-versatile";
    
    public GroqClient(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Groq API key cannot be empty");
        }
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
        this.gson = new Gson();
        logger.info("Groq client initialized with model: {}", model);
    }
    
    /**
     * Send chat request to Groq
     */
    public String chat(String userMessage) throws Exception {
        logger.info("Sending request to Groq API");
        logger.debug("User message: {}", userMessage);
        
        // Build messages array
        JsonArray messages = new JsonArray();
        
        // System message
        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", "You are a helpful AI assistant. Give concise, clear answers.");
        messages.add(systemMessage);
        
        // User message
        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        userMsg.addProperty("content", userMessage);
        messages.add(userMsg);
        
        // Build request body
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", model);
        requestBody.add("messages", messages);
        requestBody.addProperty("temperature", 0.7);
        requestBody.addProperty("max_tokens", 150); // Keep responses concise for voice
        
        // Create HTTP request
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(API_URL))
            .header("Authorization", "Bearer " + apiKey)
            .header("Content-Type", "application/json")
            .timeout(Duration.ofSeconds(30))
            .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
            .build();
        
        // Send request
        logger.debug("Sending HTTP request to Groq");
        HttpResponse<String> response = httpClient.send(
            request,
            HttpResponse.BodyHandlers.ofString()
        );
        
        // Check response status
        if (response.statusCode() != 200) {
            logger.error("Groq API error: {} - {}", response.statusCode(), response.body());
            throw new Exception("Groq API error: " + response.statusCode());
        }
        
        // Parse response
        JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);
        String answer = jsonResponse
            .getAsJsonArray("choices")
            .get(0).getAsJsonObject()
            .getAsJsonObject("message")
            .get("content").getAsString();
        
        logger.info("Received response from Groq ({} chars)", answer.length());
        logger.debug("Response: {}", answer);
        
        return answer.trim();
    }
    
    /**
     * Test connection
     */
    public boolean testConnection() {
        try {
            String response = chat("Say 'OK' if you can hear me.");
            return response != null && !response.trim().isEmpty();
        } catch (Exception e) {
            logger.error("Connection test failed", e);
            return false;
        }
    }
    
    // Getters and setters
    public void setModel(String model) {
        this.model = model;
        logger.info("Model changed to: {}", model);
    }
    
    public String getModel() {
        return model;
    }
}