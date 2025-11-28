package com.mohnish.voiceassistant.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {

    private static final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);
    private static final Properties properties = new Properties();

    // Possible locations for config
    private static final String[] CONFIG_LOCATIONS = {
            "config/assistant.properties",
            "voice-assistant/config/assistant.properties",
            "src/main/resources/assistant.properties"
    };

    static {
        loadProperties();
    }

    // ============================================================
    //  LOAD assistant.properties
    // ============================================================
    private static void loadProperties() {
        for (String path : CONFIG_LOCATIONS) {
            File file = new File(path);
            if (file.exists()) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    properties.load(fis);
                    logger.info("✅ Loaded config from: {}", file.getAbsolutePath());
                    return;
                } catch (IOException e) {
                    logger.warn("❌ Failed to load config from: {}", path);
                }
            }
        }

        logger.warn("⚠️ assistant.properties NOT FOUND — Using DEFAULT configuration.");
        setDefaults();
    }

    // ============================================================
    //  DEFAULT VALUES
    // ============================================================
    private static void setDefaults() {
        properties.setProperty("tts.voice", "Samantha");
        properties.setProperty("tts.rate", "200");

        properties.setProperty("wake.word.enabled", "true");
        properties.setProperty("wake.word.path", "voice-assistant/lib/porcupine/samantha_en_mac_v3_0_0.ppn");
        properties.setProperty("wake.word.sensitivity", "0.5");

        properties.setProperty("audio.recording.duration", "5");

        properties.setProperty("show.performance.stats", "true");

        // LLM defaults
        properties.setProperty("llm.primary", "groq");
        properties.setProperty("llm.groq.model", "llama-3.3-70b-versatile");
        properties.setProperty("llm.ollama.model", "llama3.2:3b");
        properties.setProperty("ollama.url", "http://localhost:11434");

        // RAG defaults
        properties.setProperty("chroma.url", "http://localhost:8000");
        properties.setProperty("chroma.collection", "knowledge_base");
    }

    // ============================================================
    //  BASIC GETTERS
    // ============================================================
    public static String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public static String get(String key) {
        return properties.getProperty(key);
    }

    public static int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(properties.getProperty(key, Integer.toString(defaultValue)));
        } catch (Exception e) {
            logger.warn("Invalid int for {}. Using default {}", key, defaultValue);
            return defaultValue;
        }
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        return Boolean.parseBoolean(properties.getProperty(key, Boolean.toString(defaultValue)));
    }

    // ============================================================
    //  TTS
    // ============================================================
    public static String getTTSVoice() { return get("tts.voice", "Samantha"); }
    public static int getTTSRate() { return getInt("tts.rate", 200); }

    // ============================================================
    //  LLM SETTINGS
    // ============================================================
    public static String getPrimaryLLM() { return get("llm.primary", "groq"); }
    public static String getGroqModel() { return get("llm.groq.model", "llama-3.3-70b-versatile"); }
    public static String getOllamaModel() { return get("llm.ollama.model", "llama3.2:3b"); }
    public static String getOllamaURL() { return get("ollama.url", "http://localhost:11434"); }

    // ============================================================
    //  RECORDING
    // ============================================================
    public static int getRecordingDuration() { return getInt("audio.recording.duration", 5); }
    public static boolean shouldShowPerformanceStats() { return getBoolean("show.performance.stats", true); }

    // ============================================================
    //  RAG / CHROMA DB
    // ============================================================
    public static String getChromaDBUrl() {
        return get("chroma.url", "http://localhost:8000");
    }

    public static String getCollectionName() {
        return get("chroma.collection", "knowledge_base");
    }

    // ============================================================
    //  PICOVOICE ACCESS KEY
    // ============================================================
    public static String getPicovoiceAccessKey() {
        String[] secretLocations = {
                "voice-assistant/config/secrets.properties",
                "config/secrets.properties"
        };

        for (String path : secretLocations) {
            File file = new File(path);
            if (file.exists()) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    Properties secrets = new Properties();
                    secrets.load(fis);

                    String key = secrets.getProperty("PICOVOICE_ACCESS_KEY");
                    if (key != null && !key.trim().isEmpty()) {
                        logger.info("🔑 Loaded Picovoice key from {}", file.getAbsolutePath());
                        return key.trim();
                    }
                } catch (Exception ignore) { }
            }
        }

        // Fallback env var
        String envKey = System.getenv("PICOVOICE_ACCESS_KEY");
        if (envKey != null && !envKey.trim().isEmpty()) {
            return envKey.trim();
        }

        throw new RuntimeException(
                "❌ Picovoice Access Key NOT FOUND!\n" +
                "Create secrets.properties in:\n" +
                "  • voice-assistant/config/secrets.properties\n" +
                "or set environment variable PICOVOICE_ACCESS_KEY."
        );
    }

    // ============================================================
    //  WAKE WORD
    // ============================================================
    public static boolean isWakeWordEnabled() {
        return getBoolean("wake.word.enabled", true);
    }

    public static String getWakeWordPath() {
        return get(
                "wake.word.path",
                "voice-assistant/lib/porcupine/samantha_en_mac_v3_0_0.ppn"
        );
    }

    public static float getWakeWordSensitivity() {
        try {
            return Float.parseFloat(get("wake.word.sensitivity", "0.5"));
        } catch (Exception e) {
            logger.warn("Invalid wake.word.sensitivity — using 0.5");
            return 0.5f;
        }
    }

    // ============================================================
    //  PRINT CONFIG
    // ============================================================
    public static void printConfig() {
        System.out.println("\n📋 Current Configuration:");
        System.out.println("  TTS Voice            : " + getTTSVoice());
        System.out.println("  TTS Rate             : " + getTTSRate());
        System.out.println("  Primary LLM          : " + getPrimaryLLM());
        System.out.println("  Ollama URL           : " + getOllamaURL());
        System.out.println("  Recording Duration   : " + getRecordingDuration() + " sec");
        System.out.println("  Performance Stats    : " + shouldShowPerformanceStats());

        System.out.println("\n🔔 Wake Word:");
        System.out.println("  Enabled              : " + isWakeWordEnabled());
        if (isWakeWordEnabled()) {
            System.out.println("  Path                 : " + getWakeWordPath());
            System.out.println("  Sensitivity          : " + getWakeWordSensitivity());
        }

        System.out.println("\n📚 RAG:");
        System.out.println("  Chroma URL           : " + getChromaDBUrl());
        System.out.println("  Collection           : " + getCollectionName());

        System.out.println();
    }
}
