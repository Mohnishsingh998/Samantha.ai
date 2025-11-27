package com.mohnish.voiceassistant.wakeword;

import com.mohnish.voiceassistant.utils.ConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class WakeWordTest {
    private static final Logger logger = LoggerFactory.getLogger(WakeWordTest.class);

    public static void main(String[] args) {
        logger.info("=== Wake Word Detection Test ===");

        try {
            // Check if wake word feature is enabled
            if (!ConfigLoader.isWakeWordEnabled()) {
                logger.warn("Wake word detection is disabled in assistant.properties");
                System.out.println("⚠️  Enable wake word in config/assistant.properties first");
                return;
            }

            // Load config
            String accessKey = ConfigLoader.getPicovoiceAccessKey();
            String keywordPath = ConfigLoader.getWakeWordPath();
            float sensitivity = ConfigLoader.getWakeWordSensitivity();

            // Mask access key safely
            String maskedKey = accessKey.length() > 10
                    ? accessKey.substring(0, 10) + "..."
                    : accessKey;

            logger.info("Access Key: {}", maskedKey);
            logger.info("Keyword Path: {}", keywordPath);
            logger.info("Sensitivity: {}", sensitivity);

            // Validate keyword file exists
            File file = new File(keywordPath);
            if (!file.exists()) {
                logger.error("Wake word model file not found: {}", file.getAbsolutePath());
                System.out.println("❌ Wake word file not found!");
                System.out.println("   Expected: " + file.getAbsolutePath());
                System.out.println("   Please download your .ppn file from Picovoice Console");
                return;
            }

            // Create detector
            PorcupineWakeWord wakeWord = new PorcupineWakeWord(
                accessKey,
                keywordPath,
                sensitivity
            );

            // Add shutdown hook for safe cleanup
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("🔻 Shutting down wake word engine...");
                wakeWord.stopListening();
                wakeWord.cleanup();  // ✅ Changed from release() to cleanup()
            }));

            // Initialize engine
            wakeWord.initialize();

            // Start listening
            logger.info("\n🎤 Listening for wake word...");
            logger.info("Say: 'Hey Samantha' (or your custom wake word)");
            logger.info("Press Ctrl+C to exit\n");

            wakeWord.startListening(() -> {
                logger.info("🎤 Wake word detected!");
                System.out.println("\n✅ WAKE WORD DETECTED! ✅\n");
                // Add chime sound or trigger assistant logic here
            });

            // Keep the program alive
            Thread.sleep(Long.MAX_VALUE);

        } catch (InterruptedException e) {
            logger.info("Test interrupted");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.error("Test failed", e);
            e.printStackTrace();
        }
    }
}