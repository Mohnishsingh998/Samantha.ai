package com.mohnish.voiceassistant.audio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public class MacOSSTTEngine {
    private static final Logger logger = LoggerFactory.getLogger(MacOSSTTEngine.class);
    private final Path tempDir;
    
    public MacOSSTTEngine() throws IOException {
        // Create temp directory for audio files
        this.tempDir = Files.createTempDirectory("voice-assistant-stt");
        logger.info("MacOS STT Engine initialized");
        logger.info("Using macOS built-in speech recognition");
    }
    
    /**
     * Transcribe audio data to text using macOS speech recognition
     */
    public String transcribe(byte[] audioData) {
        File tempAudioFile = null;
        
        try {
            // Save audio to temporary WAV file
            tempAudioFile = File.createTempFile("audio-", ".wav", tempDir.toFile());
            try (FileOutputStream fos = new FileOutputStream(tempAudioFile)) {
                // Write WAV header
                writeWavHeader(fos, audioData.length);
                // Write audio data
                fos.write(audioData);
            }
            
            logger.info("Transcribing audio file: {}", tempAudioFile.getName());
            
            // Convert WAV to text using macOS speech recognition
            String text = recognizeSpeech(tempAudioFile);
            
            logger.info("Transcription: '{}'", text);
            return text;
            
        } catch (Exception e) {
            logger.error("Transcription failed", e);
            return "";
        } finally {
            // Clean up temp file
            if (tempAudioFile != null && tempAudioFile.exists()) {
                tempAudioFile.delete();
            }
        }
    }
    
    /**
     * Use macOS speech recognition to convert audio file to text
     */
    private String recognizeSpeech(File audioFile) throws IOException, InterruptedException {
        // Method 1: Try using 'sox' and 'speech-recognition' if available
        // Method 2: Fall back to Python script using speech_recognition
        
        // For now, we'll use a Python script with speech_recognition library
        // First, check if we can use it
        
        File scriptFile = createPythonScript();
        
        ProcessBuilder pb = new ProcessBuilder(
            "python3",
            scriptFile.getAbsolutePath(),
            audioFile.getAbsolutePath()
        );
        
        pb.redirectErrorStream(true);
        Process process = pb.start();
        
        // Read output
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        
        // Wait for completion
        boolean finished = process.waitFor(30, TimeUnit.SECONDS);
        
        if (!finished) {
            process.destroy();
            throw new IOException("Speech recognition timed out");
        }
        
        if (process.exitValue() != 0) {
            logger.error("Recognition failed: {}", output.toString());
            return "";
        }
        
        // Clean up script
        scriptFile.delete();
        
        // Parse output (format: "RESULT: text here")
        String result = output.toString().trim();
        if (result.startsWith("RESULT:")) {
            return result.substring(7).trim();
        }
        
        return result.trim();
    }
    
    /**
     * Create Python script for speech recognition
     */
    private File createPythonScript() throws IOException {
        File script = File.createTempFile("stt-", ".py", tempDir.toFile());
        
        String pythonCode = 
            "#!/usr/bin/env python3\n" +
            "import sys\n" +
            "import speech_recognition as sr\n" +
            "\n" +
            "try:\n" +
            "    recognizer = sr.Recognizer()\n" +
            "    audio_file = sys.argv[1]\n" +
            "    \n" +
            "    with sr.AudioFile(audio_file) as source:\n" +
            "        audio = recognizer.record(source)\n" +
            "    \n" +
            "    # Use Google Speech Recognition (free, no API key needed)\n" +
            "    text = recognizer.recognize_google(audio)\n" +
            "    print(f'RESULT: {text}')\n" +
            "    sys.exit(0)\n" +
            "    \n" +
            "except sr.UnknownValueError:\n" +
            "    print('RESULT: ')\n" +
            "    sys.exit(0)\n" +
            "except Exception as e:\n" +
            "    print(f'ERROR: {e}', file=sys.stderr)\n" +
            "    sys.exit(1)\n";
        
        Files.writeString(script.toPath(), pythonCode);
        script.setExecutable(true);
        
        return script;
    }
    
    /**
     * Write WAV file header
     */
    private void writeWavHeader(FileOutputStream fos, int audioDataLength) throws IOException {
        int sampleRate = 16000;
        int channels = 1;
        int bitsPerSample = 16;
        
        int byteRate = sampleRate * channels * bitsPerSample / 8;
        int blockAlign = channels * bitsPerSample / 8;
        
        ByteArrayOutputStream header = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(header);
        
        // RIFF header
        dos.writeBytes("RIFF");
        dos.writeInt(Integer.reverseBytes(36 + audioDataLength));
        dos.writeBytes("WAVE");
        
        // fmt chunk
        dos.writeBytes("fmt ");
        dos.writeInt(Integer.reverseBytes(16)); // chunk size
        dos.writeShort(Short.reverseBytes((short) 1)); // PCM
        dos.writeShort(Short.reverseBytes((short) channels));
        dos.writeInt(Integer.reverseBytes(sampleRate));
        dos.writeInt(Integer.reverseBytes(byteRate));
        dos.writeShort(Short.reverseBytes((short) blockAlign));
        dos.writeShort(Short.reverseBytes((short) bitsPerSample));
        
        // data chunk
        dos.writeBytes("data");
        dos.writeInt(Integer.reverseBytes(audioDataLength));
        
        fos.write(header.toByteArray());
    }
    
    /**
     * Clean up resources
     */
    public void close() {
        try {
            // Delete temp directory
            Files.walk(tempDir)
                .sorted((a, b) -> -a.compareTo(b))
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        logger.warn("Failed to delete temp file: {}", path);
                    }
                });
            
            logger.info("STT Engine closed");
        } catch (IOException e) {
            logger.error("Error cleaning up temp directory", e);
        }
    }
}