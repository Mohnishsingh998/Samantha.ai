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
                System.out.println("   Set: wake.word.enabled=true");
                return;
            }

            // Load config
            String accessKey = ConfigLoader.getPicovoiceAccessKey();
            String keywordPath = "voice-assistant/lib/porcupine/samantha_en_mac_v3_0_0.ppn";
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
                System.out.println("\n❌ Wake word file not found!");
                System.out.println("   Expected: " + file.getAbsolutePath());
                System.out.println("\n📝 To fix this:");
                System.out.println("   1. Go to: https://console.picovoice.ai/ppn");
                System.out.println("   2. Train a wake word (e.g., 'hey samantha')");
                System.out.println("   3. Download the .ppn file");
                System.out.println("   4. Place it in: " + new File(keywordPath).getParent());
                return;
            }

            logger.info("✅ Wake word file found!");

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
                wakeWord.release();;  // ✅ FIX: Changed from release() to cleanup()
            }));

            // Initialize engine
            wakeWord.initialize();

            // Start listening
            logger.info("\n🎤 Listening for wake word...");
            String wakeWordName = new File(keywordPath)
                .getName()
                .replace(".ppn", "")
                .replace("_", " ");
            logger.info("Say: '{}'", wakeWordName);
            logger.info("Press Ctrl+C to exit\n");

            wakeWord.startListening(() -> {
                logger.info("🎤 Wake word detected!");
                System.out.println("\n✅ ============ WAKE WORD DETECTED! ============ ✅\n");
                // Add chime sound or trigger assistant logic here
            });

            // Keep the program alive
            Thread.sleep(Long.MAX_VALUE);

        } catch (InterruptedException e) {
            logger.info("Test interrupted");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.error("Test failed", e);
            System.err.println("\n❌ ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
}