package com.mohnish.voiceassistant.audio;

import java.util.Scanner;

public class MacOSSTTTest {
    public static void main(String[] args) {
        System.out.println("🗣️  macOS Speech-to-Text Test\n");
        
        MicrophoneCapture mic = new MicrophoneCapture();
        MacOSSTTEngine stt = null;
        
        try {
            // Initialize components
            mic.initialize();
            stt = new MacOSSTTEngine();
            
            Scanner scanner = new Scanner(System.in);
            
            System.out.println("✅ Ready to recognize speech!");
            System.out.println("   Using Google Speech Recognition (via Python)");
            System.out.println();
            
            while (true) {
                System.out.print("Press ENTER to record (or 'q' to quit): ");
                String input = scanner.nextLine();
                
                if (input.equalsIgnoreCase("q")) {
                    break;
                }
                
                // Record audio
                System.out.println("\n🔴 RECORDING for 5 seconds... Speak now!");
                byte[] audioData = mic.record(5);
                
                // Transcribe
                System.out.println("🔄 Transcribing...");
                long startTime = System.currentTimeMillis();
                String text = stt.transcribe(audioData);
                long duration = System.currentTimeMillis() - startTime;
                
                // Display result
                if (text.isEmpty()) {
                    System.out.println("⚠️  No speech detected or couldn't recognize");
                } else {
                    System.out.println("📝 You said: \"" + text + "\"");
                    System.out.println("⏱️  Transcription took: " + duration + "ms");
                }
                
                System.out.println();
            }
            
            System.out.println("✅ Test completed!");
            
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            mic.stop();
            if (stt != null) stt.close();
        }
    }
}