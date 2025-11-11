package com.mohnish.voiceassistant.document;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Extracts text and metadata from supported document formats (.pdf, .txt)
 */
public class DocumentParser {
    private static final Logger logger = LoggerFactory.getLogger(DocumentParser.class);

    /**
     * Extract text from a PDF file
     */
    public String extractTextFromPDF(File pdfFile) throws IOException {
        logger.info("Extracting text from PDF: {}", pdfFile.getName());

        if (!pdfFile.exists()) {
            throw new IOException("File not found: " + pdfFile.getAbsolutePath());
        }

        if (!pdfFile.getName().toLowerCase().endsWith(".pdf")) {
            throw new IOException("Not a PDF file: " + pdfFile.getName());
        }

        try (PDDocument document = PDDocument.load(pdfFile)) {
            // Handle encrypted PDFs
            if (document.isEncrypted()) {
                logger.warn("PDF is encrypted: {}", pdfFile.getName());
                try {
                    document.setAllSecurityToBeRemoved(true);
                } catch (Exception e) {
                    throw new IOException("Cannot decrypt PDF: " + pdfFile.getName(), e);
                }
            }

            // Extract text
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String text = stripper.getText(document);

            // Clean and analyze text
            text = cleanText(text);
            int pageCount = document.getNumberOfPages();
            int wordCount = text.isEmpty() ? 0 : text.split("\\s+").length;

            logger.info("✅ Extracted {} pages (~{} words) from {}", pageCount, wordCount, pdfFile.getName());

            return text;
        } catch (IOException e) {
            logger.error("❌ Failed to extract text from PDF: {}", pdfFile.getName(), e);
            throw e;
        }
    }

    /**
     * Extract text from a plain text file
     */
    public String extractTextFromTxt(File txtFile) throws IOException {
        logger.info("Reading text file: {}", txtFile.getName());

        if (!txtFile.exists()) {
            throw new IOException("File not found: " + txtFile.getAbsolutePath());
        }

        String text = Files.readString(txtFile.toPath());
        text = cleanText(text);

        int wordCount = text.isEmpty() ? 0 : text.split("\\s+").length;
        logger.info("✅ Read {} words from {}", wordCount, txtFile.getName());

        return text;
    }

    /**
     * Auto-detect and extract text from any supported file
     */
    public String extractText(File file) throws IOException {
        String filename = file.getName().toLowerCase();

        logger.info("Detecting file type for: {}", file.getName());

        if (filename.endsWith(".pdf")) {
            return extractTextFromPDF(file);
        } else if (filename.endsWith(".txt")) {
            return extractTextFromTxt(file);
        } else {
            throw new IOException("Unsupported file type: " + filename +
                    "\nSupported types: .pdf, .txt");
        }
    }

    /**
     * Clean and normalize extracted text
     */
    private String cleanText(String text) {
          if (text == null || text.trim().isEmpty()) {
              return "";
          }
          
          // 1. Normalize whitespace
          text = text.replaceAll("\\s+", " ");
          
          // 2. Remove page numbers and headers
          text = text.replaceAll("(?m)^\\s*\\d+\\s*$", "");
          text = text.replaceAll("(?m)^\\s*Page \\d+\\s*$", "");
          text = text.replaceAll("(?m)^\\s*Chapter \\d+\\s*$", "");
          
          // 3. Remove URLs and emails
          text = text.replaceAll("https?://\\S+", "");
          text = text.replaceAll("[\\w.-]+@[\\w.-]+\\.\\w+", "");
          
          // 4. Remove common PDF artifacts
          text = text.replaceAll("\\(cid:\\d+\\)", ""); // PDF encoding artifacts
          text = text.replaceAll("\\u00A0", " "); // Non-breaking spaces
          
          // 5. Fix common OCR errors (optional)
          text = text.replaceAll("\\bl\\b", "I"); // lowercase L → I
          text = text.replaceAll("\\b0\\b", "O"); // zero → O (context dependent)
          
          // 6. Remove excessive line breaks
          text = text.replaceAll("\n{3,}", "\n\n");
          
          // 7. Fix spacing around punctuation
          text = text.replaceAll("\\s+([.,!?;:])", "$1");
          text = text.replaceAll("([.,!?;:])([A-Z])", "$1 $2");
          
          // 8. Trim
          text = text.trim();
          
          return text;
      }

    /**
     * Extract metadata for a given file
     */
    public DocumentMetadata getMetadata(File file) throws IOException {
        DocumentMetadata metadata = new DocumentMetadata();
        metadata.setFilename(file.getName());
        metadata.setFilePath(file.getAbsolutePath());
        metadata.setFileSize(file.length());

        if (file.getName().toLowerCase().endsWith(".pdf")) {
            try (PDDocument document = PDDocument.load(file)) {
                metadata.setPageCount(document.getNumberOfPages());

                PDDocumentInformation info = document.getDocumentInformation();
                if (info != null) {
                    String title = info.getTitle();
                    metadata.setTitle(title != null ? title : file.getName());
                    metadata.setAuthor(info.getAuthor());
                    metadata.setSubject(info.getSubject());
                } else {
                    metadata.setTitle(file.getName());
                }
            }
        } else {
            metadata.setTitle(file.getName());
            metadata.setPageCount(1);
        }

        logger.info("📄 Metadata extracted for {} -> {} pages, size: {} bytes",
                metadata.getFilename(), metadata.getPageCount(), metadata.getFileSize());

        return metadata;
    }
}
