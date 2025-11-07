package com.mohnish.voiceassistant.audio;

import java.util.Scanner;

public class TTSTest {
    public static void main(String[] args) {
        System.out.println("🔊 Text-to-Speech Test\n");
        
        Scanner scanner = new Scanner(System.in);
        
        // List available voices
        System.out.println("Listing available macOS voices...\n");
        MacOSTTSEngine.listVoices();
        
        // Choose voice
        System.out.print("Enter voice name (or press ENTER for 'Samantha'): ");
        String voiceName = scanner.nextLine().trim();
        if (voiceName.isEmpty()) {
            voiceName = "Samantha";
        }
        
        System.out.print("Enter speech rate (or press ENTER for 200): ");
        String rateInput = scanner.nextLine().trim();
        int rate = 200;
        if (!rateInput.isEmpty()) {
            try {
                rate = Integer.parseInt(rateInput);
            } catch (NumberFormatException e) {
                System.out.println("Invalid rate, using default: 200");
            }
        }
        
        MacOSTTSEngine tts = new MacOSTTSEngine(voiceName, rate);
        
        // Test pre-defined sentences
        String[] testSentences = {
            "Hello, I am your personal AI assistant.",
            "I can answer questions from your books and notes.",
            "Machine learning is a subset of artificial intelligence.",
            "The quick brown fox jumps over the lazy dog.",
            "Welcome to the voice assistant test program."
        };
        
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║   Testing Pre-defined Sentences        ║");
        System.out.println("╚════════════════════════════════════════╝");
        
        for (int i = 0; i < testSentences.length; i++) {
            System.out.println("\n[Test " + (i + 1) + "] " + testSentences[i]);
            System.out.print("Press ENTER to hear it...");
            scanner.nextLine();
            
            System.out.println("🔊 Speaking...");
            tts.speak(testSentences[i]);
            System.out.println("✅ Done");
        }
        
        // Interactive mode
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║        Interactive Mode                ║");
        System.out.println("╚════════════════════════════════════════╝");
        System.out.println("Enter text to speak (or 'q' to quit):\n");
        
        while (true) {
            System.out.print("💬 Text: ");
            String input = scanner.nextLine();
            
            if (input.equalsIgnoreCase("q")) {
                break;
            }
            
            if (!input.trim().isEmpty()) {
                System.out.println("🔊 Speaking...");
                tts.speak(input);
                System.out.println("✅ Done\n");
            }
        }
        
        System.out.println("\n✅ TTS Test completed!");
    }
}