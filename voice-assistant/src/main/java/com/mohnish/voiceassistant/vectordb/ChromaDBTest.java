package com.mohnish.voiceassistant.vectordb;

import java.util.*;

public class ChromaDBTest {
    public static void main(String[] args) {
        System.out.println("🔍 ChromaDB Client Test (v2 API)\n");
        
        ChromaDBClient client = new ChromaDBClient("http://localhost:8000");
        
        try {
            // Test 1: Connection
            System.out.println("Test 1: Testing connection...");
            if (client.testConnection()) {
                System.out.println("✅ Connected to ChromaDB v2\n");
            } else {
                System.err.println("❌ Connection failed!");
                System.err.println("Make sure ChromaDB is running:");
                System.err.println("  chromadb run --path ./chroma_data --port 8000");
                System.exit(1);
            }
            
            // Test 2: Create collection
            System.out.println("Test 2: Creating test collection...");
            String collectionName = "test_collection";
            client.createCollection(collectionName);
            System.out.println("✅ Collection created/exists\n");
            
            // Test 3: Add dummy documents
            System.out.println("Test 3: Adding test documents...");
            
            List<String> ids = Arrays.asList("doc1", "doc2", "doc3");
            
            // Dummy 768-dimensional embeddings (matching nomic-embed-text)
            List<List<Double>> embeddings = Arrays.asList(
                generateDummyEmbedding(768),
                generateDummyEmbedding(768),
                generateDummyEmbedding(768)
            );
            
            List<String> documents = Arrays.asList(
                "Machine learning is a subset of artificial intelligence.",
                "Neural networks are inspired by biological neurons.",
                "Deep learning uses multiple layers of neural networks."
            );
            
            List<Map<String, String>> metadatas = Arrays.asList(
                Map.of("source", "AI Textbook", "page", "1", "chunk", "0"),
                Map.of("source", "AI Textbook", "page", "15", "chunk", "1"),
                Map.of("source", "AI Textbook", "page", "42", "chunk", "2")
            );
            
            client.addDocuments(collectionName, ids, embeddings, documents, metadatas);
            System.out.println("✅ Documents added\n");
            
            // Test 4: Query
            System.out.println("Test 4: Querying collection...");
            List<Double> queryEmbedding = generateDummyEmbedding(768);
            List<ChromaDBClient.QueryResult> results = client.query(collectionName, queryEmbedding, 2);
            
            System.out.println("Top 2 results:");
            for (ChromaDBClient.QueryResult result : results) {
                System.out.println("  - " + result);
                System.out.println("    Metadata: " + result.getMetadata());
            }
            System.out.println();
            
            // Test 5: List collections
            System.out.println("Test 5: Listing all collections...");
            List<String> collections = client.listCollections();
            System.out.println("Collections: " + collections);
            System.out.println();
            
            // Test 6: Clean up
            System.out.println("Test 6: Cleaning up...");
            client.deleteCollection(collectionName);
            System.out.println("✅ Collection deleted\n");
            
            System.out.println("═".repeat(50));
            System.out.println("🎉 All tests passed!");
            System.out.println("═".repeat(50));
            System.out.println("\nYour ChromaDB client is ready to use!");
            
        } catch (Exception e) {
            System.err.println("❌ Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Generate dummy embedding for testing
     */
    private static List<Double> generateDummyEmbedding(int dimensions) {
        List<Double> embedding = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < dimensions; i++) {
            embedding.add(random.nextDouble() * 2 - 1); // Random between -1 and 1
        }
        return embedding;
    }
}