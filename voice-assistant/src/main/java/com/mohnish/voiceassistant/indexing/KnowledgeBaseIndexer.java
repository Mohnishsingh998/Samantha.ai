package com.mohnish.voiceassistant.indexing;

import com.mohnish.voiceassistant.document.DocumentChunk;
import com.mohnish.voiceassistant.document.DocumentParser;
import com.mohnish.voiceassistant.document.TextChunker;
import com.mohnish.voiceassistant.embedding.EmbeddingGenerator;
import com.mohnish.voiceassistant.utils.ConfigLoader;
import com.mohnish.voiceassistant.vectordb.ChromaDBClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

public class KnowledgeBaseIndexer {

    private static final Logger logger = LoggerFactory.getLogger(KnowledgeBaseIndexer.class);

    private final DocumentParser parser;
    private final TextChunker chunker;
    private final EmbeddingGenerator embeddingGenerator;
    private final ChromaDBClient chromaClient;
    private final String collectionName;

    public KnowledgeBaseIndexer(String chromaUrl, String ollamaUrl, String collectionName) {
        this.parser = new DocumentParser();
        this.chunker = new TextChunker();
        this.embeddingGenerator = new EmbeddingGenerator(ollamaUrl);
        this.chromaClient = new ChromaDBClient(chromaUrl);
        this.collectionName = collectionName;

        logger.info("📚 Knowledge Base Indexer initialized");
        logger.info("   • ChromaDB : {}", chromaUrl);
        logger.info("   • Ollama   : {}", ollamaUrl);
        logger.info("   • Collection: {}", collectionName);
    }

    // ---------------------------------------------------------
    //  INDEXING LOGIC
    // ---------------------------------------------------------

    public void initializeCollection() throws Exception {
        logger.info("⚙ Initializing collection...");
        chromaClient.createCollection(collectionName);
        logger.info("✅ Collection is ready");
    }

    public IndexingResult indexBook(File bookFile) throws Exception {
        logger.info("----------------------------------------------------");
        logger.info("📘 Indexing book: {}", bookFile.getName());

        long startTime = System.currentTimeMillis();
        IndexingResult result = new IndexingResult(bookFile.getName());

        try {
            // Extract text
            logger.info("1️⃣ Extracting text...");
            String text = parser.extractText(bookFile);
            result.setCharactersExtracted(text.length());
            logger.info("   ✔ Extracted {} characters", text.length());

            // Chunk text
            logger.info("2️⃣ Chunking text...");
            List<DocumentChunk> chunks = chunker.chunkText(text, bookFile.getName());
            result.setChunksCreated(chunks.size());
            logger.info("   ✔ Created {} chunks", chunks.size());

            // Generate embeddings
            logger.info("3️⃣ Generating embeddings (this may take time)...");
            List<List<Double>> embeddings = embeddingGenerator.generateForChunks(chunks);
            result.setEmbeddingsGenerated(embeddings.size());
            logger.info("   ✔ Generated {} embeddings", embeddings.size());

            // Store in Chroma
            logger.info("4️⃣ Storing in ChromaDB...");
            storeChunksInChroma(chunks, embeddings);
            result.setChunksStored(chunks.size());
            logger.info("   ✔ Stored {} chunks", chunks.size());

            long duration = System.currentTimeMillis() - startTime;
            result.setDurationMs(duration);
            result.setSuccess(true);

            logger.info("🎉 Finished indexing {} in {}ms", bookFile.getName(), duration);
            return result;

        } catch (Exception e) {
            logger.error("❌ Failed to index book {}", bookFile.getName(), e);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
            throw e;
        }
    }

    private void storeChunksInChroma(List<DocumentChunk> chunks, List<List<Double>> embeddings)
            throws Exception {

        if (chunks.size() != embeddings.size()) {
            throw new IllegalArgumentException("Chunks and embeddings size mismatch");
        }

        List<String> ids = new ArrayList<>();
        List<String> documents = new ArrayList<>();
        List<Map<String, String>> metadatas = new ArrayList<>();

        for (int i = 0; i < chunks.size(); i++) {
            DocumentChunk c = chunks.get(i);

            ids.add(c.getId());
            documents.add(c.getText());

            Map<String, String> meta = new HashMap<>();
            meta.put("source", c.getSourceFile());
            meta.put("chunk_index", String.valueOf(c.getChunkIndex()));

            metadatas.add(meta);
        }

        chromaClient.addDocuments(collectionName, ids, embeddings, documents, metadatas);
    }

    public List<IndexingResult> indexBooks(List<File> bookFiles) throws Exception {
        logger.info("📚 Indexing {} books", bookFiles.size());
        List<IndexingResult> results = new ArrayList<>();

        for (File file : bookFiles) {
            results.add(indexBook(file));
        }
        return results;
    }

    public List<IndexingResult> indexDirectory(String directoryPath) throws Exception {
        File dir = new File(directoryPath);
        if (!dir.exists()) throw new IllegalArgumentException("Directory not found: " + directoryPath);

        File[] pdfs = dir.listFiles((d, n) -> n.toLowerCase().endsWith(".pdf") || n.endsWith(".txt"));
        if (pdfs == null || pdfs.length == 0) {
            logger.warn("⚠ No PDF/TXT files in directory {}", directoryPath);
            return new ArrayList<>();
        }

        logger.info("📁 Found {} files to index", pdfs.length);
        return indexBooks(List.of(pdfs));
    }

    // ---------------------------------------------------------
    //  MAIN METHOD (needed for Maven exec)
    // ---------------------------------------------------------

    public static void main(String[] args) {
        try {
            logger.info("====================================================");
            logger.info("🚀 STARTING KNOWLEDGE BASE INDEXER");

            // Load assistant.properties
            // Read config values
            String chromaUrl = ConfigLoader.getChromaDBUrl();
            String ollamaUrl = ConfigLoader.getOllamaURL();
            String collection = ConfigLoader.getCollectionName();

            logger.info("Chroma URL   : {}", chromaUrl);
            logger.info("Ollama URL   : {}", ollamaUrl);
            logger.info("Collection   : {}", collection);

            String booksDir = "books"; // default

            KnowledgeBaseIndexer indexer =
                    new KnowledgeBaseIndexer(chromaUrl, ollamaUrl, collection);

            indexer.initializeCollection();
            indexer.indexDirectory(booksDir);

            logger.info("🎉 INDEXING COMPLETE");
            logger.info("====================================================");

        } catch (Exception e) {
            logger.error("❌ Indexer failed: {}", e.getMessage(), e);
        }
    }
}
