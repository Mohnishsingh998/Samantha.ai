package com.mohnish.voiceassistant.wakeword;

import ai.picovoice.porcupine.Porcupine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicBoolean;

public class PorcupineWakeWord {

    private static final Logger logger = LoggerFactory.getLogger(PorcupineWakeWord.class);

    private Porcupine porcupine;
    private TargetDataLine microphone;
    private Thread listeningThread;

    private final AtomicBoolean isListening = new AtomicBoolean(false);

    private final String accessKey;
    private final String keywordPath;
    private final float sensitivity;

    // Callback for wake-word detection
    private Runnable onWakeWordDetected;

    public PorcupineWakeWord(String accessKey, String keywordPath, float sensitivity) {
        this.accessKey = accessKey;
        this.keywordPath = keywordPath;
        this.sensitivity = sensitivity;
    }

    /**
     * Initialize Porcupine and verify resources.
     */
    public void initialize() throws Exception {
        logger.info("Initializing Porcupine...");

        File file = new File(keywordPath);
        if (!file.exists()) {
            throw new IllegalArgumentException("Keyword .ppn file not found: " + file.getAbsolutePath());
        }

        porcupine = new Porcupine.Builder()
                .setAccessKey(accessKey)
                .setKeywordPath(keywordPath)
                .setSensitivity(sensitivity)
                .build();

        logger.info("Porcupine initialized.");
        logger.info("Sample Rate  : {}", porcupine.getSampleRate());
        logger.info("Frame Length : {}", porcupine.getFrameLength());
    }

    /**
     * Start listening in a background thread.
     */
    public void startListening(Runnable callback) {
        if (isListening.get()) {
            logger.warn("Wake-word listener already running.");
            return;
        }

        if (porcupine == null) {
            throw new IllegalStateException("Porcupine not initialized. Call initialize() first.");
        }

        this.onWakeWordDetected = callback;
        isListening.set(true);

        listeningThread = new Thread(() -> {
            try {
                listenLoop();
            } catch (Exception e) {
                logger.error("Wake-word listening thread error", e);
            }
        }, "porcupine-listener");

        listeningThread.setDaemon(true);
        listeningThread.start();

        logger.info("Listening for wake word: {}", new File(keywordPath).getName().replace(".ppn", ""));
    }

    /**
     * Internal audio capture + Porcupine processing loop.
     */
    private void listenLoop() throws Exception {

        AudioFormat format = new AudioFormat(
                porcupine.getSampleRate(),
                16,
                1,
                true,
                false
        );

        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        microphone = (TargetDataLine) AudioSystem.getLine(info);
        microphone.open(format);
        microphone.start();

        logger.info("Microphone opened.");

        int frameLength = porcupine.getFrameLength();
        byte[] byteBuffer = new byte[frameLength * 2];
        short[] pcm = new short[frameLength];

        while (isListening.get()) {
            int bytesRead = microphone.read(byteBuffer, 0, byteBuffer.length);
            if (bytesRead <= 0) continue;

            // Convert only the valid portion of bytesRead
            int samplesRead = bytesRead / 2;
            if (samplesRead < frameLength) {
                // Skip partial frame
                continue;
            }

            ByteBuffer.wrap(byteBuffer)
                    .order(ByteOrder.LITTLE_ENDIAN)
                    .asShortBuffer()
                    .get(pcm);

            int result = porcupine.process(pcm);
            if (result >= 0) {
                logger.info("🎤 Wake Word Detected!");

                if (onWakeWordDetected != null) {
                    try {
                        onWakeWordDetected.run();
                    } catch (Exception ex) {
                        logger.error("Wake-word callback error", ex);
                    }
                }

                // Avoid immediate retrigger
                Thread.sleep(800);
            }
        }

        microphone.stop();
        microphone.close();
        logger.info("Microphone closed (listening stopped).");
    }

    /**
     * Stop listening and stop the audio thread.
     */
    public void stopListening() {
        if (!isListening.get()) return;

        logger.info("Stopping wake-word listener...");
        isListening.set(false);

        try {
            if (microphone != null && microphone.isOpen()) {
                microphone.stop();
                microphone.flush();
                microphone.close();
            }
        } catch (Exception ignored) {}

        if (listeningThread != null) {
            try {
                listeningThread.join(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        logger.info("Wake-word listener stopped.");
    }

    /**
     * Free all resources.
     */
    public void release() {
        logger.info("Releasing PorcupineWakeWord resources...");
        stopListening();

        if (porcupine != null) {
            try {
                porcupine.delete();
            } catch (Exception ignored) {}
            porcupine = null;
        }

        logger.info("PorcupineWakeWord released.");
    }

    public boolean isListening() {
        return isListening.get();
    }
}
