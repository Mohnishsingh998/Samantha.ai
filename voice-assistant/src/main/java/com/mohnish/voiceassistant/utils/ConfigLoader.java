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

        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            properties.load(input);
            logger.info("Configuration loaded from: {}", CONFIG_FILE);
        } catch (IOException e) {
            logger.warn("assistant.properties not found. Using default configuration.");
            setDefaults();
        }
    }

    /**
     * Default configuration
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

        // Wake word defaults
        properties.setProperty("wake.word.enabled", "true");
        properties.setProperty("wake.word.path", "voice-assistant/lib/porcupine/samantha_en_mac_v3_0_0.ppn");
        properties.setProperty("wake.word.sensitivity", "0.5");
    }

    // Basic getters
    public static String get(String key) {
        return properties.getProperty(key);
    }

    public static String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public static int getInt(String key, int defaultValue) {
        try {
            String value = properties.getProperty(key);
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            logger.warn("Invalid integer for {}. Using default {}", key, defaultValue);
            return defaultValue;
        }
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }

    // Convenience getters
    public static String getTTSVoice() { return get("tts.voice", "Samantha"); }
    public static int getTTSRate() { return getInt("tts.rate", 200); }
    public static String getPrimaryLLM() { return get("llm.primary", "groq"); }
    public static String getGroqModel() { return get("llm.groq.model", "llama-3.3-70b-versatile"); }
    public static String getOllamaModel() { return get("llm.ollama.model", "llama3.2:3b"); }
    public static String getOllamaURL() { return get("llm.ollama.url", "http://localhost:11434"); }
    public static int getMaxTokens() { return getInt("llm.max.tokens", 150); }
    public static int getRecordingDuration() { return getInt("audio.recording.duration", 5); }
    public static boolean shouldAutoSpeakWelcome() { return getBoolean("auto.speak.welcome", true); }
    public static boolean shouldShowPerformanceStats() { return getBoolean("show.performance.stats", true); }
    public static boolean isDebugMode() { return getBoolean("debug.mode", false); }

    /**
     * Secret key loader (safe)
     */
    public static String getPicovoiceAccessKey() {

    // Get the actual working directory 
    String baseDir = System.getProperty("user.dir");

    // Correct, absolute path to secrets.properties
    String secretsPath = baseDir + "/voice-assistant/config/secrets.properties";

    System.out.println("DEBUG: Loading secrets from: " + secretsPath);

    try (InputStream input = new FileInputStream(secretsPath)) {
        Properties secrets = new Properties();
        secrets.load(input);

        String key = secrets.getProperty("PICOVOICE_ACCESS_KEY");
        if (key != null && !key.isEmpty()) {
            return key;
        }
    } catch (IOException e) {
        // Show error so user knows the file wasn't found
        System.out.println("DEBUG: Could not load secrets.properties at: " + secretsPath);
    }

    // Fallback to environment variable
    String envKey = System.getenv("PICOVOICE_ACCESS_KEY");
    if (envKey == null || envKey.isEmpty()) {
        throw new RuntimeException(
            "Picovoice access key missing.\n" +
            "Expected at: " + secretsPath + "\n" +
            "Or set environment variable PICOVOICE_ACCESS_KEY"
        );
    }

    return envKey;
}


    /**
     * Wake Word Settings
     */
    public static boolean isWakeWordEnabled() {
        return getBoolean("wake.word.enabled", true);
    }

    public static String getWakeWordPath() {
        return get("wake.word.path", "lib/porcupine/samantha_en_mac_v3_0_0.ppn");
    }

    public static float getWakeWordSensitivity() {
        try {
            return Float.parseFloat(get("wake.word.sensitivity", "0.5"));
        } catch (Exception e) {
            logger.warn("Invalid wake.word.sensitivity. Using default 0.5");
            return 0.5f;
        }
    }

    /**
     * Print configuration
     */
    public static void printConfig() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║            Current Configuration        ║");
        System.out.println("╚════════════════════════════════════════╝");

        System.out.println("TTS Voice        : " + getTTSVoice());
        System.out.println("TTS Rate         : " + getTTSRate());
        System.out.println("Primary LLM      : " + getPrimaryLLM());
        System.out.println("Groq Model       : " + getGroqModel());
        System.out.println("Ollama Model     : " + getOllamaModel());
        System.out.println("Wake Word Enabled: " + isWakeWordEnabled());
        System.out.println("Wake Word Path   : " + getWakeWordPath());
        System.out.println("Sensitivity      : " + getWakeWordSensitivity());
        System.out.println("Max Tokens       : " + getMaxTokens());
        System.out.println("Record Sec       : " + getRecordingDuration());

        System.out.println("══════════════════════════════════════════\n");
    }
}
