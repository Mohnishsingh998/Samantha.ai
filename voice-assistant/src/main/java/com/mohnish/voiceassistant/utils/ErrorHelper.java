package com.mohnish.voiceassistant.utils;

public class ErrorHelper {
    
    /**
     * Get helpful error message for common issues
     */
    public static String getHelpfulMessage(Exception e) {
        String error = e.getMessage().toLowerCase();
        
        if (error.contains("groq") || error.contains("api")) {
            return "🌐 Internet connection issue or Groq API problem.\n" +
                   "   Try: Type 'local' to use offline mode (Ollama)";
                   
        } else if (error.contains("ollama") || error.contains("11434")) {
            return "🏠 Ollama not running.\n" +
                   "   Try: Open new terminal and run: ollama serve";
                   
        } else if (error.contains("microphone") || error.contains("line unavailable")) {
            return "🎤 Microphone access issue.\n" +
                   "   Try: Check System Settings → Privacy → Microphone\n" +
                   "   Also: Close other apps using the microphone (Zoom, etc.)";
                   
        } else if (error.contains("speech") || error.contains("recognition")) {
            return "📝 Speech recognition issue.\n" +
                   "   Try: Speak more clearly, check internet connection\n" +
                   "   Or: Install: pip3 install SpeechRecognition";
                   
        } else if (error.contains("timeout")) {
            return "⏱️  Request timed out.\n" +
                   "   Try: Check internet connection, or try again";
                   
        } else {
            return "❌ " + e.getMessage() + "\n" +
                   "   Try: Restart the assistant or check logs";
        }
    }
    
    /**
     * Print helpful error
     */
    public static void printError(Exception e) {
        System.err.println("\n╔════════════════════════════════════════╗");
        System.err.println("║            Error Occurred              ║");
        System.err.println("╚════════════════════════════════════════╝");
        System.err.println(getHelpfulMessage(e));
        System.err.println("══════════════════════════════════════════\n");
    }
}