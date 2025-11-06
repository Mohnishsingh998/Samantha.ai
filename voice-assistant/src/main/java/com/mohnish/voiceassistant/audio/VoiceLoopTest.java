// package com.mohnish.voiceassistant.audio;

// import java.util.Scanner;

// public class VoiceLoopTest {
//     public static void main(String[] args) {
//         System.out.println("🎤🔊 Complete Voice Loop Test");
//         System.out.println("Speak → Text → Speak Back\n");
        
//         MicrophoneCapture mic = new MicrophoneCapture();
//         MacOSSTTEngine stt = null;
//         MacOSTTSEngine tts = new MacOSSTTEngine("Samantha", 200);
        
//         try {
//             // Initialize
//             mic.initialize();
//             stt = new MacOSSTTEngine();
            
//             Scanner scanner = new Scanner(System.in);
            
//             System.out.println("🎯 This test will:");
//             System.out.println("   1. Record your voice");
//             System.out.println("   2. Convert speech to text");
//             System.out.println("   3. Speak the text back to you\n");
            
//             while (true) {
//                 System.out.print("Press ENTER to start (or 'q' to quit): ");
//                 String input = scanner.nextLine();
                
//                 if (input.equalsIgnoreCase("q")) {
//                     break;
//                 }
                
//                 // Step 1: Record
//                 System.out.println("\n🔴 Recording for 5 seconds... Speak now!");
//                 byte[] audioData = mic.record(5);
                
//                 // Step 2: Transcribe
//                 System.out.println("🔄 Converting speech to text...");
//                 String text = stt.transcribe(audioData);
                
//                 // Step 3: Display
//                 if (text.isEmpty()) {
//                     System.out.println("⚠️  No speech detected. Please try again.");
//                     continue;
//                 }
                
//                 System.out.println("\n📝 You said: \"" + text + "\"");
                
//                 // Step 4: Speak back
//                 System.out.println("🔊 Speaking back...");
//                 tts.speak("You said: " + text);
                
//                 System.out.println("\n✅ Loop completed\n");
//             }
            
//             System.out.println("\n🎉 Test completed successfully!");
            
//         } catch (Exception e) {
//             System.err.println("❌ Error: " + e.getMessage());
//             e.printStackTrace();
//         } finally {
//             mic.stop();
//             if (stt != null) stt.close();
//         }
//     }
// }