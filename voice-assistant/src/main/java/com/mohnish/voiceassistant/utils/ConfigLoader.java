package com.mohnish.voiceassistant.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {
    private static final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);
    private static final String CONFIG_FILE = "config/assistant.properties";
    private static Properties properties;
    
    static {
        loadConfig();
    }
    
    /**
     * Load configuration from file
     */
    private static void loadConfig() {
        properties = new Properties();
        
        // Try to load from config file
        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            properties.load(input);
            logger.info("Configuration loaded from: {}", CONFIG_FILE);
        } catch (IOException e) {
            logger.warn("Could not load config file: {}. Using defaults.", CONFIG_FILE);
            setDefaults();
        }
    }
    
    /**
     * Set default values
     */
    private static void setDefaults() {
        properties.setProperty("tts.voice", "Samantha");
        properties.setProperty("tts.rate", "200");
        properties.setProperty("llm.primary", "groq");
        properties.setProperty("llm.groq.model", "llama-3.3-70b-versatile");
        properties.setProperty("llm.ollama.model", "llama3.2:3b");
        properties.setProperty("llm.ollama.url", "http://localhost:11434");
        properties.setProperty("llm.max.tokens", "150");
        properties.setProperty("audio.recording.duration", "5");
        properties.setProperty("audio.sample.rate", "16000");
        properties.setProperty("auto.speak.welcome", "true");
        properties.setProperty("show.performance.stats", "true");
        properties.setProperty("debug.mode", "false");
        properties.setProperty("log.level", "INFO");
    }
    
    /**
     * Get string property
     */
    public static String get(String key) {
        return properties.getProperty(key);
    }
    
    /**
     * Get string property with default
     */
    public static String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    /**
     * Get integer property
     */
    public static int getInt(String key, int defaultValue) {
        try {
            String value = properties.getProperty(key);
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            logger.warn("Invalid integer for key: {}. Using default: {}", key, defaultValue);
            return defaultValue;
        }
    }
    
    /**
     * Get boolean property
     */
    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }
    
    /**
     * Get TTS voice
     */
    public static String getTTSVoice() {
        return get("tts.voice", "Samantha");
    }
    
    /**
     * Get TTS rate
     */
    public static int getTTSRate() {
        return getInt("tts.rate", 200);
    }
    
    /**
     * Get primary LLM
     */
    public static String getPrimaryLLM() {
        return get("llm.primary", "groq");
    }
    
    /**
     * Get Groq model
     */
    public static String getGroqModel() {
        return get("llm.groq.model", "llama-3.3-70b-versatile");
    }
    
    /**
     * Get Ollama model
     */
    public static String getOllamaModel() {
        return get("llm.ollama.model", "llama3.2:3b");
    }
    
    /**
     * Get Ollama URL
     */
    public static String getOllamaURL() {
        return get("llm.ollama.url", "http://localhost:11434");
    }
    
    /**
     * Get max tokens
     */
    public static int getMaxTokens() {
        return getInt("llm.max.tokens", 150);
    }
    
    /**
     * Get recording duration
     */
    public static int getRecordingDuration() {
        return getInt("audio.recording.duration", 5);
    }
    
    /**
     * Should auto-speak welcome
     */
    public static boolean shouldAutoSpeakWelcome() {
        return getBoolean("auto.speak.welcome", true);
    }
    
    /**
     * Should show performance stats
     */
    public static boolean shouldShowPerformanceStats() {
        return getBoolean("show.performance.stats", true);
    }
    
    /**
     * Is debug mode enabled
     */
    public static boolean isDebugMode() {
        return getBoolean("debug.mode", false);
    }
    
    /**
     * Print current configuration
     */
    public static void printConfig() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║      Current Configuration             ║");
        System.out.println("╚════════════════════════════════════════╝");
        System.out.println("TTS Voice: " + getTTSVoice());
        System.out.println("TTS Rate: " + getTTSRate() + " wpm");
        System.out.println("Primary LLM: " + getPrimaryLLM());
        System.out.println("Groq Model: " + getGroqModel());
        System.out.println("Ollama Model: " + getOllamaModel());
        System.out.println("Max Tokens: " + getMaxTokens());
        System.out.println("Recording Duration: " + getRecordingDuration() + "s");
        System.out.println("══════════════════════════════════════════\n");
    }
}