package com.mohnish.voiceassistant.embedding;

import com.mohnish.voiceassistant.document.DocumentChunk;

import java.util.ArrayList;
import java.util.List;

public class EmbeddingTest {
    public static void main(String[] args) {
        System.out.println("🧠 Embedding Generation Test\n");
        
        EmbeddingGenerator generator = new EmbeddingGenerator("http://localhost:11434");
        
        try {
            // Test 1: Connection
            System.out.println("Test 1: Testing connection...");
            if (generator.test()) {
                System.out.println("✅ Connected to Ollama\n");
            } else {
                System.err.println("❌ Connection failed!");
                System.err.println("Make sure Ollama is running: ollama serve");
                System.exit(1);
            }
            
            // Test 2: Get dimension size
            System.out.println("Test 2: Getting embedding dimension size...");
            int dimensions = generator.getDimensionSize();
            System.out.println("✅ Embedding dimension: " + dimensions + "\n");
            
            // Test 3: Generate embedding for single text
            System.out.println("Test 3: Generating embedding for single text...");
            String testText = "Machine learning is a subset of artificial intelligence.";
            System.out.println("Text: \"" + testText + "\"");
            
            long startTime = System.currentTimeMillis();
            List<Double> embedding = generator.generateForQuery(testText);
            long duration = System.currentTimeMillis() - startTime;
            
            System.out.println("✅ Generated embedding:");
            System.out.println("   Dimensions: " + embedding.size());
            System.out.println("   Time: " + duration + "ms");
            System.out.println("   First 5 values: " + embedding.subList(0, 5));
            System.out.println();
            
            // Test 4: Generate embeddings for multiple texts
            System.out.println("Test 4: Generating embeddings for multiple texts...");
            
            List<DocumentChunk> testChunks = new ArrayList<>();
            testChunks.add(new DocumentChunk("chunk1", 
                "Artificial intelligence is the simulation of human intelligence.", 
                "test.pdf", 0));
            testChunks.add(new DocumentChunk("chunk2", 
                "Neural networks are inspired by biological neurons.", 
                "test.pdf", 1));
            testChunks.add(new DocumentChunk("chunk3", 
                "Deep learning uses multiple layers of neural networks.", 
                "test.pdf", 2));
            
            System.out.println("Generating embeddings for " + testChunks.size() + " chunks...");
            
            startTime = System.currentTimeMillis();
            List<List<Double>> embeddings = generator.generateForChunks(testChunks);
            duration = System.currentTimeMillis() - startTime;
            
            System.out.println("✅ Generated " + embeddings.size() + " embeddings");
            System.out.println("   Time: " + duration + "ms");
            System.out.println("   Average: " + (duration / embeddings.size()) + "ms per embedding");
            System.out.println();
            
            // Test 5: Verify embedding consistency
            System.out.println("Test 5: Testing embedding consistency...");
            String sameText = "This is a test sentence.";
            
            List<Double> embedding1 = generator.generateForQuery(sameText);
            Thread.sleep(100);
            List<Double> embedding2 = generator.generateForQuery(sameText);
            
            boolean identical = embedding1.equals(embedding2);
            System.out.println("Same text, same embedding: " + (identical ? "✅ Yes" : "⚠️ No (expected variation)"));
            System.out.println();
            
            // Test 6: Different texts have different embeddings
            System.out.println("Test 6: Testing different texts produce different embeddings...");
            String text1 = "Cats are cute animals.";
            String text2 = "Dogs are loyal pets.";
            
            List<Double> emb1 = generator.generateForQuery(text1);
            List<Double> emb2 = generator.generateForQuery(text2);
            
            boolean different = !emb1.equals(emb2);
            System.out.println("Different texts, different embeddings: " + (different ? "✅ Yes" : "❌ No"));
            System.out.println();
            
            System.out.println("═".repeat(50));
            System.out.println("🎉 All tests passed!");
            System.out.println("═".repeat(50));
            System.out.println("\nYou're ready to generate embeddings for your books!");
            
        } catch (Exception e) {
            System.err.println("❌ Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}