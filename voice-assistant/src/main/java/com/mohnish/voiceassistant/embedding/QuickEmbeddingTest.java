package com.mohnish.voiceassistant.embedding;

public class QuickEmbeddingTest {
    public static void main(String[] args) {
        System.out.println("🧪 Quick Embedding Test\n");
        
        EmbeddingGenerator generator = new EmbeddingGenerator("http://localhost:11434");
        
        try {
            // Test 1: Very short text
            System.out.println("Test 1: Short text");
            String shortText = "Hello world";
            System.out.println("Text: " + shortText);
            var embedding1 = generator.generateForQuery(shortText);
            System.out.println("✅ Success! Dimension: " + embedding1.size());
            System.out.println();
            
            // Test 2: Medium text
            System.out.println("Test 2: Medium text");
            String mediumText = "Machine learning is a subset of artificial intelligence that enables systems to learn and improve from experience.";
            System.out.println("Text: " + mediumText);
            var embedding2 = generator.generateForQuery(mediumText);
            System.out.println("✅ Success! Dimension: " + embedding2.size());
            System.out.println();
            
            // Test 3: Long text (similar to chunk size)
            System.out.println("Test 3: Long text (~500 tokens)");
            StringBuilder longText = new StringBuilder();
            for (int i = 0; i < 100; i++) {
                longText.append("This is sentence number ").append(i).append(". ");
            }
            System.out.println("Text length: " + longText.length() + " chars");
            var embedding3 = generator.generateForQuery(longText.toString());
            System.out.println("✅ Success! Dimension: " + embedding3.size());
            System.out.println();
            
            System.out.println("🎉 All tests passed!");
            
        } catch (Exception e) {
            System.err.println("❌ Test failed!");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}