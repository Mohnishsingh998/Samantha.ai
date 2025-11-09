package com.mohnish.voiceassistant;

import com.mohnish.voiceassistant.utils.ConfigLoader;
import com.mohnish.voiceassistant.utils.ErrorHelper;
import com.mohnish.voiceassistant.utils.PerformanceMonitor;
import com.mohnish.voiceassistant.audio.MicrophoneCapture;
import com.mohnish.voiceassistant.audio.MacOSSTTEngine;
import com.mohnish.voiceassistant.audio.MacOSTTSEngine;
import com.mohnish.voiceassistant.llm.SmartLLMRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

public class VoiceAssistant {
    private static final Logger logger = LoggerFactory.getLogger(VoiceAssistant.class);

    private MicrophoneCapture microphone;
    private MacOSSTTEngine stt;
    private MacOSTTSEngine tts;
    private SmartLLMRouter llm;
    private boolean running = false;
    private PerformanceMonitor perfMonitor = new PerformanceMonitor();

    public VoiceAssistant(String groqApiKey) throws Exception {
        logger.info("Initializing Voice Assistant...");

        // Initialize components
        this.microphone = new MicrophoneCapture();
        microphone.initialize();

        this.stt = new MacOSSTTEngine();

        // ✅ NEW: Load TTS configuration dynamically
        this.tts = new MacOSTTSEngine(
                ConfigLoader.getTTSVoice(),
                ConfigLoader.getTTSRate()
        );

        this.llm = new SmartLLMRouter(groqApiKey, "http://localhost:11434");

        logger.info("Voice Assistant initialized successfully!");
    }

    /**
     * Start the assistant in manual mode (press Enter to activate)
     */
    public void startManualMode() {
        running = true;
        Scanner scanner = new Scanner(System.in);

        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║   🤖 AI VOICE ASSISTANT - MANUAL MODE ║");
        System.out.println("╚════════════════════════════════════════╝");
        System.out.println();
        System.out.println("Press ENTER to ask a question");
        System.out.println("Type 'quit' to exit");
        System.out.println("Type 'local' to prefer local mode");
        System.out.println("Type 'cloud' to prefer cloud mode");
        System.out.println("Type 'stats' to see statistics");
        System.out.println();

        // Test connections
        llm.testConnections();

        // Welcome message
        String welcome = "Hello! I am your AI voice assistant. I can answer any question you have. Press enter to start.";
        System.out.println("🔊 " + welcome);
        tts.speak(welcome);

        int interactionCount = 0;

        while (running) {
            System.out.println("\n" + "═".repeat(50));
            System.out.print("> ");
            String command = scanner.nextLine().trim().toLowerCase();

            // Handle commands
            if (command.equals("quit") || command.equals("q")) {
                System.out.println("👋 Goodbye!");
                tts.speak("Goodbye! It was great talking with you.");
                break;

            } else if (command.equals("local")) {
                llm.setPreferLocal(true);
                System.out.println("✅ Switched to local mode (Ollama)");
                tts.speak("Switched to local mode");
                continue;

            } else if (command.equals("cloud")) {
                llm.setPreferLocal(false);
                System.out.println("✅ Switched to cloud mode (Groq)");
                tts.speak("Switched to cloud mode");
                continue;

            } else if (command.equals("stats")) {
                String stats = llm.getStats();
                System.out.println("📊 " + stats);
                continue;

            } else if (command.equals("help") || command.equals("?")) {
                printHelp();
                continue;
            }else if (command.equals("config")) {
                ConfigLoader.printConfig();
                continue;
            }else if (command.equals("perf") || command.equals("performance")) {
                perfMonitor.printSummary();
                continue;
            }

            // Process voice interaction
            try {
                interactionCount++;
                processVoiceInteraction(interactionCount);
            } catch (Exception e) {
                logger.error("Error during voice interaction", e);
                ErrorHelper.printError(e);
                tts.speak("Sorry, I encountered an error. Please try again.");
            }
        }

        cleanup();
    }

    /**
     * Process one complete voice interaction cycle
     */
    private void processVoiceInteraction(int interactionNumber) throws Exception {
        System.out.println("\n🎤 Interaction #" + interactionNumber);
        System.out.println("─".repeat(50));

        // Step 1: Listen
        int duration = ConfigLoader.getRecordingDuration(); // ✅ dynamic duration
        System.out.println("🔴 LISTENING (" + duration + " seconds)... Speak your question!");
        tts.speak("I'm listening");

        long totalStart = System.currentTimeMillis();
        byte[] audioData = microphone.record(duration);

        // Step 2: Transcribe
        System.out.println("🔄 Converting speech to text...");
        long startTime = System.currentTimeMillis();
        String question = stt.transcribe(audioData);
        long sttTime = System.currentTimeMillis() - startTime;

        if (question == null || question.trim().isEmpty()) {
            System.out.println("⚠️  No speech detected. Please try again.");
            tts.speak("I didn't hear anything. Please try again.");
            return;
        }

        System.out.println("📝 You asked: \"" + question + "\"");
        System.out.println("   ⏱️  Transcription: " + sttTime + "ms");

        // Step 3: Get AI response
        System.out.println("🤔 Thinking...");
        startTime = System.currentTimeMillis();
        String answer = llm.generate(question);
        long llmTime = System.currentTimeMillis() - startTime;

        System.out.println("\n💡 ANSWER:");
        System.out.println("   " + answer);
        System.out.println("   ⏱️  AI processing: " + llmTime + "ms");

        // Step 4: Speak answer
        System.out.println("\n🔊 Speaking answer...");
        startTime = System.currentTimeMillis();
        tts.speak(answer);
        long ttsTime = System.currentTimeMillis() - startTime;

        long totalTime = System.currentTimeMillis() - totalStart;

        // ✅ Only show performance stats if enabled
        if (ConfigLoader.shouldShowPerformanceStats()) {
            System.out.println("\n📊 Performance:");
            System.out.println("   • STT: " + sttTime + "ms");
            System.out.println("   • AI:  " + llmTime + "ms");
            System.out.println("   • TTS: " + ttsTime + "ms");
            System.out.println("   • Total: " + totalTime + "ms");
        }
        perfMonitor.recordInteraction(sttTime, llmTime, ttsTime, totalTime);
        System.out.println("\n✅ Interaction complete!");
    }

    /**
     * Print help message
     */
    private void printHelp() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║         VOICE ASSISTANT HELP           ║");
        System.out.println("╠════════════════════════════════════════╣");
        System.out.println("║ COMMANDS:                              ║");
        System.out.println("║  ENTER  - Ask a question (voice)      ║");
        System.out.println("║  local  - Use Ollama (offline)         ║");
        System.out.println("║  cloud  - Use Groq (online, default)   ║");
        System.out.println("║  stats  - Show usage statistics        ║");
        System.out.println("║  config - Show configuration           ║");
        System.out.println("║  help   - Show this help               ║");
        System.out.println("║  quit   - Exit assistant               ║");
        System.out.println("╠════════════════════════════════════════╣");
        System.out.println("║ TIPS:                                  ║");
        System.out.println("║  • Speak clearly and at normal pace   ║");
        System.out.println("║  • Quiet environment = better accuracy ║");
        System.out.println("║  • Ask any question - I'm smart! 🧠   ║");
        System.out.println("║  • If slow, try 'local' mode          ║");
        System.out.println("╠════════════════════════════════════════╣");
        System.out.println("║ TROUBLESHOOTING:                       ║");
        System.out.println("║  • No response? Check microphone       ║");
        System.out.println("║  • Slow? Check internet connection     ║");
        System.out.println("║  • Error? Try restarting assistant     ║");
        System.out.println("╚════════════════════════════════════════╝\n");
    }

    /**
     * Clean up resources
     */
    private void cleanup() {
        logger.info("Cleaning up resources...");

        if (microphone != null) {
            microphone.stop();
        }

        if (stt != null) {
            stt.close();
        }

        logger.info("Cleanup complete");
    }

    /**
     * Main entry point
     */
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║   🤖 AI VOICE ASSISTANT - MVP 1.0       ║");
        System.out.println("║      Powered by Groq & Ollama           ║");
        System.out.println("╚══════════════════════════════════════════╝\n");

        // Check for API key
        String groqApiKey = System.getenv("GROQ_API_KEY");
        if (groqApiKey == null || groqApiKey.trim().isEmpty()) {
            System.err.println("❌ ERROR: GROQ_API_KEY environment variable not set!");
            System.err.println("\nPlease set it by running:");
            System.err.println("  export GROQ_API_KEY='your-api-key-here'");
            System.err.println("\nGet your API key from: https://console.groq.com");
            System.exit(1);
        }

        // ✅ Print current config before startup
        ConfigLoader.printConfig();

        try {
            // Create and start assistant
            VoiceAssistant assistant = new VoiceAssistant(groqApiKey);
            assistant.startManualMode();

        } catch (Exception e) {
            logger.error("Fatal error", e);
            System.err.println("\n❌ FATAL ERROR: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
