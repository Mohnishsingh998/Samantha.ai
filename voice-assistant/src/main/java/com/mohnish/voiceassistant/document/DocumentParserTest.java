package com.mohnish.voiceassistant.document;

import java.io.File;

public class DocumentParserTest {
    public static void main(String[] args) {
        System.out.println("📄 Document Parser Test\n");
        
        DocumentParser parser = new DocumentParser();
        File booksDir = new File("books");
        
        if (!booksDir.exists() || !booksDir.isDirectory()) {
            System.err.println("❌ 'books' directory not found!");
            System.err.println("Please create 'books' folder and add some PDF/TXT files.");
            System.exit(1);
        }
        
        File[] files = booksDir.listFiles((dir, name) -> 
            name.toLowerCase().endsWith(".pdf") || name.toLowerCase().endsWith(".txt")
        );
        
        if (files == null || files.length == 0) {
            System.err.println("❌ No PDF or TXT files found in 'books' directory!");
            System.err.println("Please add at least one PDF or TXT file.");
            System.exit(1);
        }
        
        System.out.println("Found " + files.length + " document(s)\n");
        
        for (File file : files) {
            System.out.println("═".repeat(60));
            System.out.println("Processing: " + file.getName());
            System.out.println("═".repeat(60));
            
            try {
                // Extract metadata
                DocumentMetadata metadata = parser.getMetadata(file);
                System.out.println("\n📋 Metadata:");
                System.out.println("  " + metadata);
                if (metadata.getAuthor() != null) {
                    System.out.println("  Author: " + metadata.getAuthor());
                }
                if (metadata.getSubject() != null) {
                    System.out.println("  Subject: " + metadata.getSubject());
                }
                System.out.println("  Size: " + metadata.getFileSizeFormatted());
                
                // Extract text
                System.out.println("\n📄 Extracting text...");
                long startTime = System.currentTimeMillis();
                String text = parser.extractText(file);
                long duration = System.currentTimeMillis() - startTime;
                
                // Show statistics
                int charCount = text.length();
                int wordCount = text.split("\\s+").length;
                int paragraphCount = text.split("\n\n").length;
                
                System.out.println("\n📊 Statistics:");
                System.out.println("  Characters: " + String.format("%,d", charCount));
                System.out.println("  Words: " + String.format("%,d", wordCount));
                System.out.println("  Paragraphs: " + paragraphCount);
                System.out.println("  Extraction time: " + duration + "ms");
                
                // Show first 500 characters
                System.out.println("\n📖 Preview (first 500 chars):");
                System.out.println("─".repeat(60));
                String preview = text.substring(0, Math.min(500, text.length()));
                System.out.println(preview);
                if (text.length() > 500) {
                    System.out.println("... [" + (text.length() - 500) + " more characters]");
                }
                System.out.println("─".repeat(60));
                
                System.out.println("\n✅ Successfully parsed!");
                
            } catch (Exception e) {
                System.err.println("\n❌ Error: " + e.getMessage());
                e.printStackTrace();
            }
            
            System.out.println();
        }
        
        System.out.println("═".repeat(60));
        System.out.println("🎉 Test completed!");
        System.out.println("═".repeat(60));
    }
}