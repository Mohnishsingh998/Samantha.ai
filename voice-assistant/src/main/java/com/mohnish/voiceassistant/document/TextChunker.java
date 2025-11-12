package com.mohnish.voiceassistant.document;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Splits text into semantic chunks for RAG.
 */
public class TextChunker {
    private static final Logger logger = LoggerFactory.getLogger(TextChunker.class);

    private int chunkSize = 500;           // Target chunk size in words
    private int chunkOverlap = 50;         // Overlap between chunks
    private ChunkingStrategy strategy;

    public enum ChunkingStrategy {
        FIXED_SIZE,        // Fixed number of words
        SENTENCE_BOUNDARY, // Split on sentence boundaries
        PARAGRAPH_BOUNDARY // Split on paragraph boundaries
    }

    public TextChunker() {
        this.strategy = ChunkingStrategy.SENTENCE_BOUNDARY;
    }

    public TextChunker(int chunkSize, int chunkOverlap, ChunkingStrategy strategy) {
        this.chunkSize = chunkSize;
        this.chunkOverlap = chunkOverlap;
        this.strategy = strategy;
    }

    /**
     * Chunk text from a document.
     */
    public List<DocumentChunk> chunkDocument(String text, String sourceFile, String documentTitle) {
        if (sourceFile == null) sourceFile = "unknown_source";

        logger.info("Chunking document: {} (strategy: {})", sourceFile, strategy);
        List<DocumentChunk> chunks = new ArrayList<>();

        if (text == null || text.trim().isEmpty()) {
            logger.warn("Empty text provided for chunking");
            return chunks;
        }

        switch (strategy) {
            case FIXED_SIZE:
                chunks = chunkByFixedSize(text, sourceFile, documentTitle);
                break;
            case SENTENCE_BOUNDARY:
                chunks = chunkBySentence(text, sourceFile, documentTitle);
                break;
            case PARAGRAPH_BOUNDARY:
                chunks = chunkByParagraph(text, sourceFile, documentTitle);
                break;
        }

        logger.info("Created {} chunks from {}", chunks.size(), sourceFile);
        return chunks;
    }

    /**
     * Chunk by fixed word count.
     */
    private List<DocumentChunk> chunkByFixedSize(String text, String sourceFile, String documentTitle) {
        List<DocumentChunk> chunks = new ArrayList<>();
        String[] words = text.split("\\s+");

        int chunkIndex = 0;
        int startPos = 0;
        int pageNumber = -1;  // Optional metadata
        String chapter = null;

        for (int i = 0; i < words.length; i += (chunkSize - chunkOverlap)) {
            int end = Math.min(i + chunkSize, words.length);

            // Build chunk text
            StringBuilder chunkText = new StringBuilder();
            for (int j = i; j < end; j++) {
                if (j > i) chunkText.append(" ");
                chunkText.append(words[j]);
            }

            String chunkContent = chunkText.toString();
            if (chunkContent.trim().isEmpty()) continue;

            int endPos = startPos + chunkContent.length();

            String chunkId = generateChunkId(sourceFile, chunkIndex);
            DocumentChunk chunk = new DocumentChunk(
                    chunkId, chunkContent, sourceFile, documentTitle,
                    chunkIndex, startPos, endPos
            );

            // --- ✅ Add metadata ---
            chunk.addMetadata("word_count", String.valueOf(chunk.getWordCount()));
            chunk.addMetadata("char_count", String.valueOf(chunk.getCharCount()));
            chunk.addMetadata("strategy", strategy.name());
            chunk.addMetadata("chunk_size_config", String.valueOf(chunkSize));
            chunk.addMetadata("overlap_config", String.valueOf(chunkOverlap));

            if (pageNumber > 0) {
                chunk.addMetadata("page", String.valueOf(pageNumber));
            }
            if (chapter != null) {
                chunk.addMetadata("chapter", chapter);
            }

            chunks.add(chunk);
            chunkIndex++;
            startPos = endPos;
        }

        return chunks;
    }

    /**
     * Chunk by sentence boundaries (recommended).
     */
    private List<DocumentChunk> chunkBySentence(String text, String sourceFile, String documentTitle) {
        List<DocumentChunk> chunks = new ArrayList<>();
        List<String> sentences = splitIntoSentences(text);

        if (sentences.isEmpty()) {
            return chunks;
        }

        List<String> currentChunk = new ArrayList<>();
        int currentWordCount = 0;
        int chunkIndex = 0;
        int startPos = 0;
        int pageNumber = -1;  // Optional metadata
        String chapter = null;

        for (String sentence : sentences) {
            int sentenceWordCount = sentence.split("\\s+").length;

            // If adding this sentence exceeds chunk size, save current chunk
            if (currentWordCount + sentenceWordCount > chunkSize && !currentChunk.isEmpty()) {
                String chunkText = String.join(" ", currentChunk);
                int endPos = startPos + chunkText.length();

                String chunkId = generateChunkId(sourceFile, chunkIndex);
                DocumentChunk chunk = new DocumentChunk(
                        chunkId, chunkText, sourceFile, documentTitle,
                        chunkIndex, startPos, endPos
                );

                // --- ✅ Add metadata ---
                chunk.addMetadata("word_count", String.valueOf(chunk.getWordCount()));
                chunk.addMetadata("char_count", String.valueOf(chunk.getCharCount()));
                chunk.addMetadata("strategy", strategy.name());
                chunk.addMetadata("chunk_size_config", String.valueOf(chunkSize));
                chunk.addMetadata("overlap_config", String.valueOf(chunkOverlap));

                if (pageNumber > 0) {
                    chunk.addMetadata("page", String.valueOf(pageNumber));
                }
                if (chapter != null) {
                    chunk.addMetadata("chapter", chapter);
                }

                chunks.add(chunk);

                // Handle overlap
                int overlapSentences = calculateOverlapSentences(currentChunk);
                if (overlapSentences > 0) {
                    List<String> keepSentences = currentChunk.subList(
                            currentChunk.size() - overlapSentences,
                            currentChunk.size()
                    );
                    currentChunk = new ArrayList<>(keepSentences);
                    currentWordCount = countWords(keepSentences);
                } else {
                    currentChunk.clear();
                    currentWordCount = 0;
                }

                chunkIndex++;
                startPos = endPos;
            }

            currentChunk.add(sentence);
            currentWordCount += sentenceWordCount;
        }

        // Add remaining chunk
        if (!currentChunk.isEmpty()) {
            String chunkText = String.join(" ", currentChunk);
            int endPos = startPos + chunkText.length();

            String chunkId = generateChunkId(sourceFile, chunkIndex);
            DocumentChunk chunk = new DocumentChunk(
                    chunkId, chunkText, sourceFile, documentTitle,
                    chunkIndex, startPos, endPos
            );

            // --- ✅ Add metadata for final chunk ---
            chunk.addMetadata("word_count", String.valueOf(chunk.getWordCount()));
            chunk.addMetadata("char_count", String.valueOf(chunk.getCharCount()));
            chunk.addMetadata("strategy", strategy.name());
            chunk.addMetadata("chunk_size_config", String.valueOf(chunkSize));
            chunk.addMetadata("overlap_config", String.valueOf(chunkOverlap));

            if (pageNumber > 0) {
                chunk.addMetadata("page", String.valueOf(pageNumber));
            }
            // if (chapter != null) {
            //     chunk.addMetadata("chapter", chapter);
            // }

            chunks.add(chunk);
        }

        return chunks;
    }

    /**
     * Chunk by paragraph boundaries.
     */
    private List<DocumentChunk> chunkByParagraph(String text, String sourceFile, String documentTitle) {
        List<DocumentChunk> chunks = new ArrayList<>();
        String[] paragraphs = text.split("\n\n+");

        List<String> currentChunk = new ArrayList<>();
        int currentWordCount = 0;
        int chunkIndex = 0;
        int startPos = 0;
        int pageNumber = -1;  // Optional metadata
        String chapter = null;

        for (String paragraph : paragraphs) {
            paragraph = paragraph.trim();
            if (paragraph.isEmpty()) continue;

            int paragraphWordCount = paragraph.split("\\s+").length;

            if (currentWordCount + paragraphWordCount > chunkSize && !currentChunk.isEmpty()) {
                String chunkText = String.join("\n\n", currentChunk);
                int endPos = startPos + chunkText.length();

                String chunkId = generateChunkId(sourceFile, chunkIndex);
                DocumentChunk chunk = new DocumentChunk(
                        chunkId, chunkText, sourceFile, documentTitle,
                        chunkIndex, startPos, endPos
                );

                // --- ✅ Add metadata ---
                chunk.addMetadata("word_count", String.valueOf(chunk.getWordCount()));
                chunk.addMetadata("char_count", String.valueOf(chunk.getCharCount()));
                chunk.addMetadata("strategy", strategy.name());
                chunk.addMetadata("chunk_size_config", String.valueOf(chunkSize));
                chunk.addMetadata("overlap_config", String.valueOf(chunkOverlap));

                if (pageNumber > 0) {
                    chunk.addMetadata("page", String.valueOf(pageNumber));
                }
                if (chapter != null) {
                    chunk.addMetadata("chapter", chapter);
                }

                chunks.add(chunk);

                currentChunk.clear();
                currentWordCount = 0;
                chunkIndex++;
                startPos = endPos;
            }

            currentChunk.add(paragraph);
            currentWordCount += paragraphWordCount;
        }

        if (!currentChunk.isEmpty()) {
            String chunkText = String.join("\n\n", currentChunk);
            int endPos = startPos + chunkText.length();

            String chunkId = generateChunkId(sourceFile, chunkIndex);
            DocumentChunk chunk = new DocumentChunk(
                    chunkId, chunkText, sourceFile, documentTitle,
                    chunkIndex, startPos, endPos
            );

            // --- ✅ Add metadata for final chunk ---
            chunk.addMetadata("word_count", String.valueOf(chunk.getWordCount()));
            chunk.addMetadata("char_count", String.valueOf(chunk.getCharCount()));
            chunk.addMetadata("strategy", strategy.name());
            chunk.addMetadata("chunk_size_config", String.valueOf(chunkSize));
            chunk.addMetadata("overlap_config", String.valueOf(chunkOverlap));

            if (pageNumber > 0) {
                chunk.addMetadata("page", String.valueOf(pageNumber));
            }
            // if (chapter != null) {
            //     chunk.addMetadata("chapter", chapter);
            // }

            chunks.add(chunk);
        }

        return chunks;
    }

    /**
     * Split text into sentences using regex.
     */
    private List<String> splitIntoSentences(String text) {
        List<String> sentences = new ArrayList<>();
        Pattern pattern = Pattern.compile("[^.!?]+[.!?]+\\s*|.+$");
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            String sentence = matcher.group().trim();
            if (!sentence.isEmpty()) {
                sentences.add(sentence);
            }
        }

        return sentences;
    }

    private int calculateOverlapSentences(List<String> sentences) {
        int overlapWords = 0;
        int sentencesToKeep = 0;

        for (int i = sentences.size() - 1; i >= 0; i--) {
            int sentenceWords = sentences.get(i).split("\\s+").length;
            if (overlapWords + sentenceWords <= chunkOverlap) {
                overlapWords += sentenceWords;
                sentencesToKeep++;
            } else {
                break;
            }
        }

        return sentencesToKeep;
    }

    private int countWords(List<String> strings) {
        int total = 0;
        for (String s : strings) {
            total += s.split("\\s+").length;
        }
        return total;
    }

    private String generateChunkId(String sourceFile, int chunkIndex) {
        String cleanName = sourceFile.replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();
        return String.format("%s_chunk_%04d", cleanName, chunkIndex);
    }

    // Getters and setters
    public int getChunkSize() { return chunkSize; }
    public void setChunkSize(int chunkSize) { this.chunkSize = chunkSize; }

    public int getChunkOverlap() { return chunkOverlap; }
    public void setChunkOverlap(int chunkOverlap) { this.chunkOverlap = chunkOverlap; }

    public ChunkingStrategy getStrategy() { return strategy; }
    public void setStrategy(ChunkingStrategy strategy) { this.strategy = strategy; }
}
