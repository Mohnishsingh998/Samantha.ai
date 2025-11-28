package com.mohnish.voiceassistant;

import com.mohnish.voiceassistant.utils.ConfigLoader;
import com.mohnish.voiceassistant.utils.ErrorHelper;
import com.mohnish.voiceassistant.utils.PerformanceMonitor;
import com.mohnish.voiceassistant.audio.MicrophoneCapture;
import com.mohnish.voiceassistant.audio.MacOSSTTEngine;
import com.mohnish.voiceassistant.audio.MacOSTTSEngine;
import com.mohnish.voiceassistant.llm.SmartLLMRouter;
import com.mohnish.voiceassistant.wakeword.PorcupineWakeWord;
import com.mohnish.voiceassistant.rag.RAGPipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

public class VoiceAssistant {
    private static final Logger logger = LoggerFactory.getLogger(VoiceAssistant.class);

    private MicrophoneCapture microphone;
    private MacOSSTTEngine stt;
    private MacOSTTSEngine tts;
    private SmartLLMRouter llm;
    private RAGPipeline ragPipeline;
    private PorcupineWakeWord wakeWord;
    
    private boolean running = false;
    private volatile boolean isProcessingQuery = false;
    private PerformanceMonitor perfMonitor = new PerformanceMonitor();
    
    private boolean useRAG = false;  // Toggle between LLM and RAG mode

    public VoiceAssistant(String groqApiKey) throws Exception {
        logger.info("Initializing Voice Assistant...");

        // Initialize audio components
        this.microphone = new MicrophoneCapture();
        microphone.initialize();

        this.stt = new MacOSSTTEngine();

        // Load TTS configuration dynamically
        this.tts = new MacOSTTSEngine(
                ConfigLoader.getTTSVoice(),
                ConfigLoader.getTTSRate()
        );

        // Initialize LLM router
        this.llm = new SmartLLMRouter(groqApiKey, "http://localhost:11434");

        logger.info("Voice Assistant initialized successfully!");
    }

    /**
     * Initialize wake word detection (call this separately if you want wake word mode)
     */
    public void initializeWakeWord() throws Exception {
        if (ConfigLoader.isWakeWordEnabled()) {
            logger.info("Initializing wake word detection...");
            
            String accessKey = ConfigLoader.getPicovoiceAccessKey();
            String keywordPath = ConfigLoader.getWakeWordPath();
            float sensitivity = ConfigLoader.getWakeWordSensitivity();
            
            this.wakeWord = new PorcupineWakeWord(accessKey, keywordPath, sensitivity);
            wakeWord.initialize();
            
            logger.info("Wake word detection initialized successfully!");
        } else {
            logger.info("Wake word detection disabled in config");
        }
    }

    /**
     * Initialize RAG pipeline (call this if you want to use book knowledge)
     */
    public void initializeRAG() throws Exception {
        logger.info("Initializing RAG pipeline...");
        
        // ✅ FIX: Get required configuration parameters
        String chromaUrl = ConfigLoader.getChromaDBUrl();
        String ollamaUrl = ConfigLoader.getOllamaURL();
        String collectionName = ConfigLoader.getCollectionName();
        String groqApiKey = System.getenv("GROQ_API_KEY");
        
        logger.info("RAG Configuration:");
        logger.info("  ChromaDB: {}", chromaUrl);
        logger.info("  Ollama: {}", ollamaUrl);
        logger.info("  Collection: {}", collectionName);
        
        // ✅ FIX: Pass required parameters to RAGPipeline constructor
        this.ragPipeline = new RAGPipeline(chromaUrl, ollamaUrl, collectionName, groqApiKey);
        this.useRAG = true;
        
        logger.info("RAG pipeline initialized successfully!");
    }

    /**
     * Start the assistant in WAKE WORD mode (hands-free)
     */
    public void startWakeWordMode() {
        if (wakeWord == null) {
            logger.error("Wake word not initialized! Call initializeWakeWord() first.");
            System.err.println("❌ Wake word detection not initialized!");
            return;
        }

        running = true;

        System.out.println("\n╔═══════════════════════════════════════════╗");
        System.out.println("║   🤖 AI VOICE ASSISTANT - WAKE WORD MODE ║");
        System.out.println("╚═══════════════════════════════════════════╝");
        System.out.println();
        System.out.println("🎤 Say 'Hey Mohnish' to activate");
        System.out.println("Press Ctrl+C to exit");
        System.out.println();

        if (useRAG) {
            System.out.println("📚 RAG Mode: Answers from your books");
        } else {
            System.out.println("🧠 LLM Mode: General knowledge");
        }
        System.out.println();

        // Welcome message
        String welcome = "Wake word mode activated. Say Hey Mohnish to ask me anything.";
        tts.speak(welcome);

        // Start listening for wake word
        wakeWord.startListening(() -> {
            if (!isProcessingQuery) {
                handleWakeWordDetected();
            }
        });

        // Keep running until interrupted
        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            logger.info("Wake word mode interrupted");
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Handle wake word detection
     */
    private void handleWakeWordDetected() {
        try {
            isProcessingQuery = true;
            long totalStart = System.currentTimeMillis();

            System.out.println("\n" + "═".repeat(50));
            System.out.println("🎤 Wake word detected! Listening for your question...");
            
            // Play activation feedback
            tts.speak("Yes?");
            
            // Brief pause to let user start speaking
            Thread.sleep(800);
            
            // Step 1: Capture audio
            int duration = ConfigLoader.getRecordingDuration();
            System.out.println("🔴 LISTENING (" + duration + " seconds)...");
            byte[] audioData = microphone.record(duration);
            
            // Step 2: Transcribe
            System.out.println("🔄 Converting speech to text...");
            long startTime = System.currentTimeMillis();
            String question = stt.transcribe(audioData);
            long sttTime = System.currentTimeMillis() - startTime;
            
            if (question == null || question.trim().isEmpty()) {
                System.out.println("⚠️  No speech detected.");
                tts.speak("I didn't hear anything. Please try again.");
                return;
            }
            
            System.out.println("📝 Question: \"" + question + "\"");
            System.out.println("   ⏱️  Transcription: " + sttTime + "ms");
            
            // Step 3: Get answer (RAG or direct LLM)
            System.out.println("🤔 Thinking...");
            startTime = System.currentTimeMillis();
            String answer;
            
            // ✅ FIX: Changed from query() to answer()
            if (useRAG && ragPipeline != null) {
                answer = ragPipeline.answer(question);
                System.out.println("   📚 (Answer from your books)");
            } else {
                answer = llm.generate(question);
                System.out.println("   🧠 (Answer from AI)");
            }
            
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
            
            // Show performance stats if enabled
            if (ConfigLoader.shouldShowPerformanceStats()) {
                System.out.println("\n📊 Performance:");
                System.out.println("   • STT: " + sttTime + "ms");
                System.out.println("   • AI:  " + llmTime + "ms");
                System.out.println("   • TTS: " + ttsTime + "ms");
                System.out.println("   • Total: " + totalTime + "ms");
            }
            
            perfMonitor.recordInteraction(sttTime, llmTime, ttsTime, totalTime);
            
            System.out.println("\n✅ Ready for next question");
            System.out.println("   Say 'Hey Mohnish' again to ask another question");
            
        } catch (Exception e) {
            logger.error("Error processing query", e);
            ErrorHelper.printError(e);
            tts.speak("Sorry, I encountered an error.");
        } finally {
            isProcessingQuery = false;
        }
    }

    /**
     * Start the assistant in MANUAL mode (press Enter to activate)
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
        System.out.println("Type 'rag' to toggle RAG mode");
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

            } else if (command.equals("rag")) {
                if (ragPipeline == null) {
                    System.out.println("⚠️  RAG not initialized. Initializing now...");
                    try {
                        initializeRAG();
                        System.out.println("✅ RAG mode enabled");
                        tts.speak("RAG mode enabled. I can now answer from your books.");
                    } catch (Exception e) {
                        System.out.println("❌ Failed to initialize RAG: " + e.getMessage());
                        logger.error("RAG initialization failed", e);
                    }
                } else {
                    useRAG = !useRAG;
                    System.out.println("✅ RAG mode: " + (useRAG ? "ON" : "OFF"));
                    tts.speak(useRAG ? "RAG mode on" : "RAG mode off");
                }
                continue;

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
                
            } else if (command.equals("config")) {
                ConfigLoader.printConfig();
                continue;
                
            } else if (command.equals("perf") || command.equals("performance")) {
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
     * Process one complete voice interaction cycle (for manual mode)
     */
    private void processVoiceInteraction(int interactionNumber) throws Exception {
        System.out.println("\n🎤 Interaction #" + interactionNumber);
        System.out.println("─".repeat(50));

        // Step 1: Listen
        int duration = ConfigLoader.getRecordingDuration();
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

        // Step 3: Get AI response (RAG or direct LLM)
        System.out.println("🤔 Thinking...");
        startTime = System.currentTimeMillis();
        String answer;
        
        // ✅ FIX: Changed from query() to answer()
        if (useRAG && ragPipeline != null) {
            answer = ragPipeline.answer(question);
            System.out.println("   📚 (Using RAG - answer from your books)");
        } else {
            answer = llm.generate(question);
            System.out.println("   🧠 (Using direct LLM)");
        }
        
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

        // Only show performance stats if enabled
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
        System.out.println("║  ENTER  - Ask a question (voice)       ║");
        System.out.println("║  rag    - Toggle RAG mode (books)      ║");
        System.out.println("║  local  - Use Ollama (offline)         ║");
        System.out.println("║  cloud  - Use Groq (online, default)   ║");
        System.out.println("║  stats  - Show usage statistics        ║");
        System.out.println("║  config - Show configuration           ║");
        System.out.println("║  perf   - Show performance stats       ║");
        System.out.println("║  help   - Show this help               ║");
        System.out.println("║  quit   - Exit assistant               ║");
        System.out.println("╠════════════════════════════════════════╣");
        System.out.println("║ TIPS:                                  ║");
        System.out.println("║  • Speak clearly and at normal pace    ║");
        System.out.println("║  • Quiet environment = better accuracy ║");
        System.out.println("║  • Ask any question - I'm smart! 🧠    ║");
        System.out.println("║  • Use RAG mode for book questions     ║");
        System.out.println("║  • If slow, try 'local' mode           ║");
        System.out.println("╠════════════════════════════════════════╣");
        System.out.println("║ TROUBLESHOOTING:                       ║");
        System.out.println("║  • No response? Check microphone       ║");
        System.out.println("║  • Slow? Check internet connection     ║");
        System.out.println("║  • Error? Try restarting assistant     ║");
        System.out.println("╚════════════════════════════════════════╝\n");
    }

    /**
     * Stop wake word listening
     */
    public void stopWakeWord() {
        if (wakeWord != null) {
            logger.info("Stopping wake word detection...");
            wakeWord.stopListening();
            wakeWord.release();
        }
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

        stopWakeWord();

        logger.info("Cleanup complete");
    }

    /**
     * Main entry point
     */
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║   🤖 AI VOICE ASSISTANT - MVP 2.0       ║");
        System.out.println("║   Powered by Groq, Ollama & Picovoice   ║");
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

        // Print current config
        ConfigLoader.printConfig();

        // Parse command line arguments
        String mode = "manual";  // default mode
        boolean enableRAG = false;
        
        for (String arg : args) {
            if (arg.equals("--wake-word") || arg.equals("-w")) {
                mode = "wakeword";
            } else if (arg.equals("--rag") || arg.equals("-r")) {
                enableRAG = true;
            } else if (arg.equals("--help") || arg.equals("-h")) {
                printUsage();
                System.exit(0);
            }
        }

        try {
            // Create assistant
            VoiceAssistant assistant = new VoiceAssistant(groqApiKey);
            
            // Initialize RAG if requested
            if (enableRAG) {
                System.out.println("📚 Initializing RAG pipeline...");
                assistant.initializeRAG();
            }
            
            // Initialize wake word if needed
            if (mode.equals("wakeword")) {
                assistant.initializeWakeWord();
            }
            
            // Add shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n\n👋 Shutting down...");
                assistant.cleanup();
            }));
            
            // Start in selected mode
            if (mode.equals("wakeword")) {
                assistant.startWakeWordMode();
            } else {
                assistant.startManualMode();
            }

        } catch (Exception e) {
            logger.error("Fatal error", e);
            System.err.println("\n❌ FATAL ERROR: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Print usage information
     */
    private static void printUsage() {
        System.out.println("\nUsage: java VoiceAssistant [OPTIONS]\n");
        System.out.println("Options:");
        System.out.println("  -w, --wake-word    Start in wake word mode (hands-free)");
        System.out.println("  -r, --rag          Enable RAG mode (answer from books)");
        System.out.println("  -h, --help         Show this help message");
        System.out.println("\nExamples:");
        System.out.println("  java VoiceAssistant                    # Manual mode");
        System.out.println("  java VoiceAssistant --wake-word        # Wake word mode");
        System.out.println("  java VoiceAssistant --wake-word --rag  # Wake word + RAG");
        System.out.println();
    }
}