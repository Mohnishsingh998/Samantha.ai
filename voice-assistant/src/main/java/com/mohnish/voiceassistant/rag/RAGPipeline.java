package com.mohnish.voiceassistant.rag;

import com.mohnish.voiceassistant.llm.SmartLLMRouter;
import com.mohnish.voiceassistant.vectordb.VectorStore;
import com.mohnish.voiceassistant.vectordb.VectorStore.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Retrieval-Augmented Generation (RAG) Pipeline
 * Combines vector search with LLM generation for knowledge-based Q&A
 */
public class RAGPipeline {
    private static final Logger logger = LoggerFactory.getLogger(RAGPipeline.class);
    
    private final VectorStore vectorStore;
    private final SmartLLMRouter llm;
    private final int defaultTopK;
    private final double relevanceThreshold;
    
    // Conversation history for context
    private final List<ConversationTurn> conversationHistory;
    private final int maxHistorySize;
    
    /**
     * Create RAG pipeline with default settings
     */
    public RAGPipeline(String chromaUrl, String ollamaUrl, 
                       String collectionName, String groqApiKey) {
        this(chromaUrl, ollamaUrl, collectionName, groqApiKey, 3, 0.5, 5);
    }
    
    /**
     * Create RAG pipeline with custom settings
     * 
     * @param chromaUrl ChromaDB URL
     * @param ollamaUrl Ollama URL
     * @param collectionName Collection name in ChromaDB
     * @param groqApiKey Groq API key (can be null to use only Ollama)
     * @param topK Number of chunks to retrieve
     * @param relevanceThreshold Minimum relevance score (0-1)
     * @param maxHistorySize Maximum conversation history to keep
     */
    public RAGPipeline(String chromaUrl, String ollamaUrl, 
                       String collectionName, String groqApiKey,
                       int topK, double relevanceThreshold, int maxHistorySize) {
        this.vectorStore = new VectorStore(chromaUrl, ollamaUrl, collectionName);
        this.llm = new SmartLLMRouter(groqApiKey, ollamaUrl);
        this.defaultTopK = topK;
        this.relevanceThreshold = relevanceThreshold;
        this.conversationHistory = new ArrayList<>();
        this.maxHistorySize = maxHistorySize;
        
        logger.info("RAG Pipeline initialized");
        logger.info("  Top-K: {}", topK);
        logger.info("  Relevance threshold: {}", relevanceThreshold);
        logger.info("  Max history: {}", maxHistorySize);
    }
    
    /**
     * Answer a question using RAG
     * 
     * @param question User's question
     * @return AI-generated answer based on knowledge base
     */
    public String answer(String question) {
        return answer(question, defaultTopK, true);
    }
    
    /**
     * Answer a question with custom settings
     * 
     * @param question User's question
     * @param topK Number of chunks to retrieve
     * @param includeHistory Whether to include conversation history
     * @return AI-generated answer
     */
    public String answer(String question, int topK, boolean includeHistory) {
        try {
            logger.info("Processing question: '{}'", question);
            
            // 1. Search knowledge base
            logger.info("🔍 Searching knowledge base (top {})", topK);
            List<SearchResult> results = vectorStore.search(question, topK);
            
            if (results.isEmpty()) {
                String response = "I couldn't find relevant information in my knowledge base. " +
                                "Could you rephrase your question or ask about a different topic?";
                addToHistory(question, response);
                return response;
            }
            
            // 2. Filter by relevance threshold
            List<SearchResult> relevantResults = new ArrayList<>();
            for (SearchResult result : results) {
                if (result.getRelevanceScore() >= relevanceThreshold) {
                    relevantResults.add(result);
                }
            }
            
            if (relevantResults.isEmpty()) {
                logger.warn("No results above relevance threshold ({})", relevanceThreshold);
                String response = "I found some information, but I'm not confident it's relevant. " +
                                "The highest relevance was " + 
                                String.format("%.1f%%", results.get(0).getRelevanceScore() * 100) +
                                ". Could you be more specific?";
                addToHistory(question, response);
                return response;
            }
            
            logger.info("✅ Found {} relevant passages", relevantResults.size());
            for (int i = 0; i < relevantResults.size(); i++) {
                SearchResult r = relevantResults.get(i);
                logger.info("  [{}] Relevance: %.1f%% - Source: {} (chunk {})",
                    i + 1, r.getRelevanceScore() * 100, r.getSource(), r.getChunkIndex());
            }
            
            // 3. Build context from retrieved chunks
            String context = buildContext(relevantResults);
            
            // 4. Build prompt with optional conversation history
            String prompt = buildPrompt(question, context, includeHistory);
            
            // 5. Generate answer using LLM
            logger.info("🤖 Generating answer with LLM...");
            long startTime = System.currentTimeMillis();
            
            String answer = llm.generate(prompt, null);
            
            long duration = System.currentTimeMillis() - startTime;
            logger.info("✅ Answer generated in {}ms", duration);
            
            // 6. Add to conversation history
            addToHistory(question, answer);
            
            return answer;
            
        } catch (Exception e) {
            logger.error("Error in RAG pipeline", e);
            return "I'm sorry, I encountered an error while processing your question: " + 
                   e.getMessage();
        }
    }
    
    /**
     * Build context string from search results
     */
    private String buildContext(List<SearchResult> results) {
        StringBuilder context = new StringBuilder();
        
        for (int i = 0; i < results.size(); i++) {
            SearchResult result = results.get(i);
            
            context.append("--- Context ").append(i + 1).append(" ---\n");
            context.append("Source: ").append(result.getSource());
            context.append(" (Chunk ").append(result.getChunkIndex()).append(")\n");
            context.append("Relevance: ").append(String.format("%.1f%%", 
                result.getRelevanceScore() * 100)).append("\n\n");
            context.append(result.getText()).append("\n\n");
        }
        
        return context.toString();
    }
    
    /**
     * Build complete prompt with system instructions, context, history, and question
     */
    private String buildPrompt(String question, String context, boolean includeHistory) {
        StringBuilder prompt = new StringBuilder();
        
        // System instructions
        prompt.append("You are a helpful AI assistant that answers questions based on provided context.\n\n");
        prompt.append("INSTRUCTIONS:\n");
        prompt.append("1. Answer ONLY based on the context provided below\n");
        prompt.append("2. If the context doesn't contain enough information, say so clearly\n");
        prompt.append("3. Be concise but complete in your answers\n");
        prompt.append("4. You can mention the source if relevant\n");
        prompt.append("5. If asked about something not in the context, politely decline\n\n");
        
        // Conversation history (if enabled and available)
        if (includeHistory && !conversationHistory.isEmpty()) {
            prompt.append("RECENT CONVERSATION:\n");
            for (ConversationTurn turn : conversationHistory) {
                prompt.append("User: ").append(turn.question).append("\n");
                prompt.append("Assistant: ").append(turn.answer).append("\n\n");
            }
            prompt.append("---\n\n");
        }
        
        // Context from knowledge base
        prompt.append("CONTEXT FROM KNOWLEDGE BASE:\n\n");
        prompt.append(context);
        prompt.append("---\n\n");
        
        // Current question
        prompt.append("USER QUESTION:\n");
        prompt.append(question).append("\n\n");
        
        prompt.append("YOUR ANSWER (based strictly on the context above):\n");
        
        return prompt.toString();
    }
    
    /**
     * Add question-answer pair to conversation history
     */
    private void addToHistory(String question, String answer) {
        conversationHistory.add(new ConversationTurn(question, answer));
        
        // Keep only last N turns
        while (conversationHistory.size() > maxHistorySize) {
            conversationHistory.remove(0);
        }
    }
    
    /**
     * Clear conversation history
     */
    public void clearHistory() {
        conversationHistory.clear();
        logger.info("Conversation history cleared");
    }
    
    /**
     * Get current conversation history
     */
    public List<ConversationTurn> getHistory() {
        return new ArrayList<>(conversationHistory);
    }
    
    /**
     * Test connection to all services
     */
    public boolean testConnection() {
        boolean chromaOk = vectorStore.testConnection();
        llm.testConnections();
        boolean llmOk = true;
        
        logger.info("Connection test: ChromaDB={}, LLM={}", chromaOk, llmOk);
        return chromaOk && llmOk;
    }
    
    /**
     * Set whether to prefer local LLM over cloud
     */
    public void setPreferLocal(boolean preferLocal) {
        llm.setPreferLocal(preferLocal);
    }
    
    /**
     * Simple class to store conversation turns
     */
    public static class ConversationTurn {
        public final String question;
        public final String answer;
        public final long timestamp;
        
        public ConversationTurn(String question, String answer) {
            this.question = question;
            this.answer = answer;
            this.timestamp = System.currentTimeMillis();
        }
        
        @Override
        public String toString() {
            return String.format("Q: %s\nA: %s", question, answer);
        }
    }
}