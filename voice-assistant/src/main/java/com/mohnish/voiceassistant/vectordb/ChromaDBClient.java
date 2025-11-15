package com.mohnish.voiceassistant.vectordb;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class ChromaDBClient {
    private static final Logger logger = LoggerFactory.getLogger(ChromaDBClient.class);
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    private final String baseUrl;
    private final OkHttpClient httpClient;
    private final Gson gson;
    
    // Default tenant and database for v2 API
    private static final String DEFAULT_TENANT = "default_tenant";
    private static final String DEFAULT_DATABASE = "default_database";
    
    public ChromaDBClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = new OkHttpClient.Builder()
            .retryOnConnectionFailure(true)
            .build();
        this.gson = new Gson();
        logger.info("ChromaDB client initialized (v2 API): {}", baseUrl);
    }
    
    /**
     * Test connection to ChromaDB v2 API
     */
    public boolean testConnection() {
        try {
            Request request = new Request.Builder()
                .url(baseUrl + "/api/v2")
                .get()
                .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    logger.info("ChromaDB connection test: ✅ Connected (v2 API)");
                    return true;
                }
                logger.warn("ChromaDB connection test: ❌ Failed (code: {})", response.code());
                return false;
            }
        } catch (Exception e) {
            logger.error("Connection test failed", e);
            return false;
        }
    }
    
    /**
     * Create collection using v2 API
     */
    public void createCollection(String collectionName) throws IOException {
        logger.info("Creating collection: {}", collectionName);
        
        // First, try to get existing collection
        try {
            getCollectionInfo(collectionName);
            logger.info("✅ Collection already exists: {}", collectionName);
            return;
        } catch (IOException e) {
            // Collection doesn't exist, create it
        }
        
        // Create new collection using v2 API
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("name", collectionName);
        
        JsonObject metadata = new JsonObject();
        metadata.addProperty("description", "Knowledge base collection");
        requestBody.add("metadata", metadata);
        
        // v2 API endpoint
        String url = String.format("%s/api/v2/tenants/%s/databases/%s/collections", 
            baseUrl, DEFAULT_TENANT, DEFAULT_DATABASE);
        
        RequestBody body = RequestBody.create(gson.toJson(requestBody), JSON);
        Request request = new Request.Builder()
            .url(url)
            .post(body)
            .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            
            if (response.isSuccessful()) {
                logger.info("✅ Collection created: {}", collectionName);
            } else if (response.code() == 409) {
                logger.info("✅ Collection already exists: {}", collectionName);
            } else {
                logger.error("Failed to create collection: {} - {}", response.code(), responseBody);
                throw new IOException("Failed to create collection: " + response.code() + " - " + responseBody);
            }
        }
    }
    
    /**
     * Get collection information using v2 API
     */
    private JsonObject getCollectionInfo(String collectionName) throws IOException {
        String url = String.format("%s/api/v2/tenants/%s/databases/%s/collections/%s", 
            baseUrl, DEFAULT_TENANT, DEFAULT_DATABASE, collectionName);
        
        Request request = new Request.Builder()
            .url(url)
            .get()
            .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Collection not found: " + collectionName);
            }
            
            String responseBody = response.body().string();
            return gson.fromJson(responseBody, JsonObject.class);
        }
    }
    
    /**
     * Get collection ID
     */
    private String getCollectionId(String collectionName) throws IOException {
        JsonObject collection = getCollectionInfo(collectionName);
        return collection.get("id").getAsString();
    }
    
    /**
     * Add documents with embeddings to collection (v2 API)
     */
    public void addDocuments(String collectionName, List<String> ids, 
                            List<List<Double>> embeddings, 
                            List<String> documents,
                            List<Map<String, String>> metadatas) throws IOException {
        
        logger.info("Adding {} documents to collection: {}", ids.size(), collectionName);
        
        // Ensure collection exists
        createCollection(collectionName);
        
        // Get collection ID
        String collectionId = getCollectionId(collectionName);
        
        // Build request
        JsonObject requestBody = new JsonObject();
        requestBody.add("ids", gson.toJsonTree(ids));
        requestBody.add("embeddings", gson.toJsonTree(embeddings));
        requestBody.add("documents", gson.toJsonTree(documents));
        requestBody.add("metadatas", gson.toJsonTree(metadatas));
        
        String url = String.format("%s/api/v2/tenants/%s/databases/%s/collections/%s/add", 
            baseUrl, DEFAULT_TENANT, DEFAULT_DATABASE, collectionId);
        
        RequestBody body = RequestBody.create(gson.toJson(requestBody), JSON);
        Request request = new Request.Builder()
            .url(url)
            .post(body)
            .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            
            if (response.isSuccessful()) {
                logger.info("✅ Added {} documents", ids.size());
            } else {
                logger.error("Failed to add documents: {} - {}", response.code(), responseBody);
                throw new IOException("Failed to add documents: " + response.code() + " - " + responseBody);
            }
        }
    }
    
    /**
     * Query collection by embedding (v2 API)
     */
    public List<QueryResult> query(String collectionName, List<Double> queryEmbedding, int nResults) throws IOException {
        logger.info("Querying collection: {} (top {})", collectionName, nResults);
        
        // Get collection ID
        String collectionId = getCollectionId(collectionName);
        
        // Build query
        JsonObject requestBody = new JsonObject();
        JsonArray embeddingsArray = new JsonArray();
        embeddingsArray.add(gson.toJsonTree(queryEmbedding));
        requestBody.add("query_embeddings", embeddingsArray);
        requestBody.addProperty("n_results", nResults);
        
        String url = String.format("%s/api/v2/tenants/%s/databases/%s/collections/%s/query", 
            baseUrl, DEFAULT_TENANT, DEFAULT_DATABASE, collectionId);
        
        RequestBody body = RequestBody.create(gson.toJson(requestBody), JSON);
        Request request = new Request.Builder()
            .url(url)
            .post(body)
            .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String error = response.body() != null ? response.body().string() : "Unknown";
                logger.error("Query failed: {} - {}", response.code(), error);
                throw new IOException("Query failed: " + response.code() + " - " + error);
            }
            
            String responseBody = response.body().string();
            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
            
            List<QueryResult> results = new ArrayList<>();
            
            if (!jsonResponse.has("ids") || jsonResponse.getAsJsonArray("ids").size() == 0) {
                logger.info("Query returned 0 results");
                return results;
            }
            
            JsonArray ids = jsonResponse.getAsJsonArray("ids").get(0).getAsJsonArray();
            JsonArray documents = jsonResponse.getAsJsonArray("documents").get(0).getAsJsonArray();
            JsonArray distances = jsonResponse.getAsJsonArray("distances").get(0).getAsJsonArray();
            JsonArray metadatas = jsonResponse.getAsJsonArray("metadatas").get(0).getAsJsonArray();
            
            for (int i = 0; i < ids.size(); i++) {
                Map<String, String> metadata = new HashMap<>();
                JsonElement metaElement = metadatas.get(i);
                
                if (!metaElement.isJsonNull() && metaElement.isJsonObject()) {
                    JsonObject metaObj = metaElement.getAsJsonObject();
                    for (String key : metaObj.keySet()) {
                        JsonElement value = metaObj.get(key);
                        if (!value.isJsonNull()) {
                            metadata.put(key, value.getAsString());
                        }
                    }
                }
                
                QueryResult result = new QueryResult(
                    ids.get(i).getAsString(),
                    documents.get(i).getAsString(),
                    distances.get(i).getAsDouble(),
                    metadata
                );
                results.add(result);
            }
            
            logger.info("✅ Query returned {} results", results.size());
            return results;
        }
    }
    
    /**
     * List all collections (v2 API)
     */
    public List<String> listCollections() throws IOException {
        String url = String.format("%s/api/v2/tenants/%s/databases/%s/collections", 
            baseUrl, DEFAULT_TENANT, DEFAULT_DATABASE);
        
        Request request = new Request.Builder()
            .url(url)
            .get()
            .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String error = response.body() != null ? response.body().string() : "Unknown";
                throw new IOException("Failed to list collections: " + response.code() + " - " + error);
            }
            
            String responseBody = response.body().string();
            JsonArray collections = gson.fromJson(responseBody, JsonArray.class);
            
            List<String> names = new ArrayList<>();
            for (int i = 0; i < collections.size(); i++) {
                JsonObject collection = collections.get(i).getAsJsonObject();
                names.add(collection.get("name").getAsString());
            }
            
            return names;
        }
    }
    
    /**
     * Delete a collection (v2 API)
     */
    public void deleteCollection(String collectionName) throws IOException {
        logger.info("Deleting collection: {}", collectionName);
        
        String url = String.format("%s/api/v2/tenants/%s/databases/%s/collections/%s", 
            baseUrl, DEFAULT_TENANT, DEFAULT_DATABASE, collectionName);
        
        Request request = new Request.Builder()
            .url(url)
            .delete()
            .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                logger.info("✅ Collection deleted: {}", collectionName);
            } else if (response.code() == 404) {
                logger.info("Collection doesn't exist (already deleted): {}", collectionName);
            } else {
                String error = response.body() != null ? response.body().string() : "Unknown";
                logger.error("Failed to delete: {} - {}", response.code(), error);
                throw new IOException("Failed to delete collection: " + response.code() + " - " + error);
            }
        }
    }
    
    /**
     * Query result class
     */
    public static class QueryResult {
        private final String id;
        private final String document;
        private final double distance;
        private final Map<String, String> metadata;
        
        public QueryResult(String id, String document, double distance, Map<String, String> metadata) {
            this.id = id;
            this.document = document;
            this.distance = distance;
            this.metadata = metadata;
        }
        
        public String getId() { return id; }
        public String getDocument() { return document; }
        public double getDistance() { return distance; }
        public Map<String, String> getMetadata() { return metadata; }
        
        @Override
        public String toString() {
            return String.format("QueryResult{id='%s', distance=%.4f, doc='%s...'}", 
                id, distance, document.substring(0, Math.min(50, document.length())));
        }
    }
}