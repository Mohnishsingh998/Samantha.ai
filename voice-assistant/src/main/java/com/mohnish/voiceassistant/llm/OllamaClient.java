package com.mohnish.voiceassistant.llm;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class OllamaClient {
    private static final Logger logger = LoggerFactory.getLogger(OllamaClient.class);
    
    private final String baseUrl;
    private final HttpClient httpClient;
    private final Gson gson;
    private String model = "llama3.2:3b";
    
    public OllamaClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
        this.gson = new Gson();
        logger.info("Ollama client initialized with model: {}", model);
    }
    
    /**
     * Send chat request to Ollama
     */
    public String chat(String userMessage) throws Exception {
        logger.info("Sending request to Ollama");
        logger.debug("User message: {}", userMessage);
        
        // Build prompt
        String prompt = "You are a helpful AI assistant. Give concise, clear answers.\n\n" +
                       "User: " + userMessage + "\n" +
                       "Assistant:";
        
        // Build request body
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", model);
        requestBody.addProperty("prompt", prompt);
        requestBody.addProperty("stream", false);
        
        JsonObject options = new JsonObject();
        options.addProperty("num_predict", 150); // Keep responses concise
        requestBody.add("options", options);
        
        // Create HTTP request
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/api/generate"))
            .header("Content-Type", "application/json")
            .timeout(Duration.ofSeconds(60))
            .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
            .build();
        
        // Send request
        logger.debug("Sending HTTP request to Ollama");
        HttpResponse<String> response = httpClient.send(
            request,
            HttpResponse.BodyHandlers.ofString()
        );
        
        // Check response status
        if (response.statusCode() != 200) {
            logger.error("Ollama API error: {} - {}", response.statusCode(), response.body());
            throw new Exception("Ollama API error: " + response.statusCode());
        }
        
        // Parse response
        JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);
        String answer = jsonResponse.get("response").getAsString();
        
        logger.info("Received response from Ollama ({} chars)", answer.length());
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