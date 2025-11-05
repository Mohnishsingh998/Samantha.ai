package com.mohnish.voiceassistant.audio;

import java.util.Scanner;

public class MicrophoneTest {
    public static void main(String[] args) {
        System.out.println("🎤 Microphone Capture Test\n");
        
        // List available devices
        MicrophoneCapture.listMicrophones();
        
        MicrophoneCapture mic = new MicrophoneCapture();
        
        try {
            // Initialize microphone
            mic.initialize();
            
            // Ask user to speak
            System.out.println("Press ENTER to start recording...");
            new Scanner(System.in).nextLine();
            
            // Record for 5 seconds
            System.out.println("🔴 RECORDING... Speak now!");
            byte[] audioData = mic.record(5);
            
            // Save to file
            String filename = "test-recording.wav";
            mic.saveToWav(audioData, filename);
            
            System.out.println("\n✅ Recording saved to: " + filename);
            System.out.println("   You can play it with: afplay " + filename);
            
            // Playback test
            System.out.println("\nPress ENTER to play recording...");
            new Scanner(System.in).nextLine();
            
            // Play using macOS 'afplay' command
            ProcessBuilder pb = new ProcessBuilder("afplay", filename);
            Process process = pb.start();
            process.waitFor();
            
            System.out.println("✅ Test completed successfully!");
            
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            mic.stop();
        }
    }
}