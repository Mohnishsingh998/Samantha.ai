package com.mohnish.voiceassistant.audio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MacOSTTSEngine {
    private static final Logger logger = LoggerFactory.getLogger(MacOSTTSEngine.class);
    
    private String voice = "Samantha";  // Default voice
    private int rate = 200;              // Words per minute
    
    public MacOSTTSEngine() {
        logger.info("TTS Engine initialized with voice: {}, rate: {}", voice, rate);
    }
    
    public MacOSTTSEngine(String voice, int rate) {
        this.voice = voice;
        this.rate = rate;
        logger.info("TTS Engine initialized with voice: {}, rate: {}", voice, rate);
    }
    
    /**
     * Speak text aloud using macOS say command
     */
    public void speak(String text) {
        if (text == null || text.trim().isEmpty()) {
            logger.warn("Empty text provided to TTS");
            return;
        }
        
        try {
            logger.info("Speaking: '{}'", text);
            
            // Build command
            List<String> command = new ArrayList<>();
            command.add("say");
            command.add("-v");
            command.add(voice);
            command.add("-r");
            command.add(String.valueOf(rate));
            command.add(text);
            
            // Execute command
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start();
            
            // Wait for completion
            int exitCode = process.waitFor();
            
            if (exitCode != 0) {
                logger.error("TTS command failed with exit code: {}", exitCode);
            } else {
                logger.info("TTS completed successfully");
            }
            
        } catch (Exception e) {
            logger.error("Failed to speak text", e);
        }
    }
    
    /**
     * Speak text in background (non-blocking)
     */
    public void speakAsync(String text) {
        new Thread(() -> speak(text)).start();
    }
    
    /**
     * List all available macOS voices
     */
    public static void listVoices() {
        try {
            System.out.println("\n═══════════════════════════════════════");
            System.out.println("    Available macOS Voices");
            System.out.println("═══════════════════════════════════════");
            
            ProcessBuilder pb = new ProcessBuilder("say", "-v", "?");
            Process process = pb.start();
            
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );
            
            String line;
            int count = 0;
            while ((line = reader.readLine()) != null) {
                count++;
                System.out.println(line);
            }
            
            process.waitFor();
            System.out.println("═══════════════════════════════════════");
            System.out.println("Total voices available: " + count);
            System.out.println("═══════════════════════════════════════\n");
            
        } catch (Exception e) {
            System.err.println("Failed to list voices: " + e.getMessage());
        }
    }
    
    /**
     * Test a specific voice
     */
    public static void testVoice(String voiceName, String text) {
        System.out.println("Testing voice: " + voiceName);
        MacOSTTSEngine tts = new MacOSTTSEngine(voiceName, 200);
        tts.speak(text);
    }
    
    // Getters and setters
    public void setVoice(String voice) {
        this.voice = voice;
        logger.info("Voice changed to: {}", voice);
    }
    
    public void setRate(int rate) {
        this.rate = rate;
        logger.info("Speech rate changed to: {}", rate);
    }
    
    public String getVoice() {
        return voice;
    }
    
    public int getRate() {
        return rate;
    }
}