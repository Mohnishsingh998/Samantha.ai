package com.mohnish.voiceassistant.audio;

import java.util.Scanner;

public class EnhancedVoiceLoopTest {
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║   🎙️  ENHANCED VOICE LOOP TEST       ║");
        System.out.println("║   Speak → Text → Display → Speak      ║");
        System.out.println("╚════════════════════════════════════════╝\n");
        
        MicrophoneCapture mic = new MicrophoneCapture();
        MacOSSTTEngine stt = null;
        MacOSTTSEngine tts = new MacOSTTSEngine("Samantha", 200);
        
        try {
            // Initialize
            System.out.println("🔧 Initializing components...");
            mic.initialize();
            stt = new MacOSSTTEngine();
            System.out.println("✅ All systems ready!\n");
            
            // Welcome message
            String welcome = "Hello! I am your voice assistant. Press enter to start talking with me.";
            System.out.println("🔊 " + welcome);
            tts.speak(welcome);
            
            Scanner scanner = new Scanner(System.in);
            int interactionCount = 0;
            
            while (true) {
                System.out.println("\n" + "═".repeat(50));
                System.out.print("Press ENTER to speak (or type 'q' to quit): ");
                String input = scanner.nextLine();
                
                if (input.equalsIgnoreCase("q")) {
                    break;
                }
                
                interactionCount++;
                System.out.println("\n🎤 Interaction #" + interactionCount);
                System.out.println("─".repeat(50));
                
                // Step 1: Record
                System.out.println("🔴 LISTENING (5 seconds)... Speak now!");
                tts.speak("I'm listening");
                
                long totalStart = System.currentTimeMillis();
                byte[] audioData = mic.record(5);
                System.out.println("✅ Recording captured");
                
                // Step 2: Transcribe
                System.out.println("🔄 Converting speech to text...");
                long startTime = System.currentTimeMillis();
                String text = stt.transcribe(audioData);
                long transcriptionTime = System.currentTimeMillis() - startTime;
                
                // Step 3: Check if speech detected
                if (text.isEmpty()) {
                    System.out.println("⚠️  No speech detected. Please try again.");
                    tts.speak("I didn't hear anything. Please try again.");
                    continue;
                }
                
                // Step 4: Display result
                System.out.println("\n📝 YOU SAID:");
                System.out.println("   \"" + text + "\"");
                System.out.println("   ⏱️  Transcription time: " + transcriptionTime + "ms");
                
                // Step 5: Generate response (simple for now)
                String response = generateResponse(text);
                System.out.println("\n💬 ASSISTANT:");
                System.out.println("   " + response);
                
                // Step 6: Speak response
                System.out.println("🔊 Speaking...");
                startTime = System.currentTimeMillis();
                tts.speak(response);
                long speakTime = System.currentTimeMillis() - startTime;
                
                long totalTime = System.currentTimeMillis() - totalStart;
                
                // Step 7: Stats
                System.out.println("\n📊 Performance:");
                System.out.println("   • Transcription: " + transcriptionTime + "ms");
                System.out.println("   • TTS: " + speakTime + "ms");
                System.out.println("   • Total cycle: " + totalTime + "ms");
                
                System.out.println("\n✅ Interaction complete!");
            }
            
            // Goodbye
            System.out.println("\n👋 Shutting down...");
            String goodbye = "Goodbye! It was nice talking with you. See you next time!";
            tts.speak(goodbye);
            
            System.out.println("\n📊 SESSION SUMMARY:");
            System.out.println("   Total interactions: " + interactionCount);
            System.out.println("\n🎉 Test completed successfully!");
            
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            mic.stop();
            if (stt != null) stt.close();
        }
    }
    
    /**
     * Generate a simple response based on input
     * (This will be replaced with LLM in Day 5)
     */
    private static String generateResponse(String input) {
        String lower = input.toLowerCase();
        
        // Simple pattern matching for now
        if (lower.contains("hello") || lower.contains("hi") || lower.contains("hey")) {
            return "Hello! How can I help you today?";
            
        } else if (lower.contains("how are you")) {
            return "I'm doing great! Thanks for asking. How are you?";
            
        } else if (lower.contains("your name") || lower.contains("who are you")) {
            return "I am your personal voice assistant. You can call me Assistant.";
            
        } else if (lower.contains("what can you do") || lower.contains("what do you do")) {
            return "I can listen to your voice, understand what you say, and respond back to you. Tomorrow I'll be able to answer intelligent questions when we add AI!";
            
        } else if (lower.contains("thank")) {
            return "You're very welcome! Happy to help.";
            
        } else if (lower.contains("bye") || lower.contains("goodbye")) {
            return "Goodbye! Have a wonderful day!";
            
        } else if (lower.contains("time")) {
            return "I don't have access to the current time yet, but I heard you ask about time.";
            
        } else if (lower.contains("weather")) {
            return "I can't check the weather yet, but that's a great feature to add in the future!";
            
        } else {
            // Echo back with AI preview
            return "I heard you say: " + input + ". Tomorrow when we add AI, I'll be able to answer questions intelligently!";
        }
    }
}