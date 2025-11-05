// MicrophoneCapture.java is a helper class that records audio from your microphone, stores it as bytes, and saves it as a .wav file using Java’s built-in javax.sound.sampled package.
package com.mohnish.voiceassistant.audio;
import com.mohnish.voiceassistant.utils.AudioUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;
import java.io.*;

public class MicrophoneCapture {
    private static final Logger logger = LoggerFactory.getLogger(MicrophoneCapture.class);
    
    private TargetDataLine microphone;
    private AudioFormat audioFormat;
    private boolean isRecording = false;
    
    public MicrophoneCapture() {
        this.audioFormat = AudioUtils.getStandardAudioFormat();
    }
    
    /**
     * Initialize microphone
     */
    public void initialize() throws LineUnavailableException {
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
        
        if (!AudioSystem.isLineSupported(info)) {
            throw new LineUnavailableException("Microphone not supported");
        }
        
        microphone = (TargetDataLine) AudioSystem.getLine(info);
        microphone.open(audioFormat);
        
        logger.info("Microphone initialized successfully");
        AudioUtils.printAudioFormat(audioFormat);
    }
    
    /**
     * Record audio for specified duration
     */
    public byte[] record(int durationSeconds) throws IOException {
        if (microphone == null) {
            throw new IllegalStateException("Microphone not initialized");
        }
        
        ByteArrayOutputStream audioData = new ByteArrayOutputStream();
        microphone.start();
        isRecording = true;
        
        logger.info("Recording for {} seconds...", durationSeconds);
        
        // Calculate buffer size for 0.1 second chunks
        int bufferSize = (int) (audioFormat.getSampleRate() * 
                               audioFormat.getFrameSize() * 0.1);
        byte[] buffer = new byte[bufferSize];
        
        long startTime = System.currentTimeMillis();
        long duration = durationSeconds * 1000L;
        
        while (isRecording && (System.currentTimeMillis() - startTime) < duration) {
            int bytesRead = microphone.read(buffer, 0, buffer.length);
            audioData.write(buffer, 0, bytesRead);
        }
        
        microphone.stop();
        isRecording = false;
        
        logger.info("Recording completed. Captured {} bytes", audioData.size());
        return audioData.toByteArray();
    }
    
    /**
     * Save audio to WAV file
     */
    public void saveToWav(byte[] audioData, String filename) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
        AudioInputStream audioInputStream = new AudioInputStream(
            bais,
            audioFormat,
            audioData.length / audioFormat.getFrameSize()
        );
        
        File outputFile = new File(filename);
        AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, outputFile);
        
        logger.info("Audio saved to: {}", outputFile.getAbsolutePath());
    }
    
    /**
     * Stop recording
     */
    public void stop() {
        isRecording = false;
        if (microphone != null && microphone.isOpen()) {
            microphone.stop();
            microphone.close();
        }
        logger.info("Microphone closed");
    }
    
    /**
     * List available microphones
     */
    public static void listMicrophones() {
        System.out.println("\n=== Available Audio Devices ===");
        Mixer.Info[] mixers = AudioSystem.getMixerInfo();
        
        for (Mixer.Info mixerInfo : mixers) {
            Mixer mixer = AudioSystem.getMixer(mixerInfo);
            Line.Info[] targetLines = mixer.getTargetLineInfo();
            
            if (targetLines.length > 0) {
                System.out.println("✓ " + mixerInfo.getName());
                System.out.println("  " + mixerInfo.getDescription());
            }
        }
        System.out.println("================================\n");
    }
}