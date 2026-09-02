package com.example.knowledgesphere.ai.service;

import com.example.knowledgesphere.ai.model.StructuredPage;
import com.example.knowledgesphere.ai.splitter.OverlappingTextSplitter;
import com.example.knowledgesphere.entity.Document;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class AIEmbeddingService {

    private final VectorStore vectorStore;

    private final OverlappingTextSplitter overlappingTextSplitter;

    private final HybridExtractionService hybridExtractionService;

    public void indexDocument(Document document, byte[] fileBytes) throws IOException {

        log.info("Started indexing document: {}", document.getOriginalFileName());

        // Production chunking
        List<StructuredPage> pages =
                hybridExtractionService.extract(
                        fileBytes,
                        document.getId(),
                        document.getOriginalFileName()
                );
        log.info("========== DOCLING RESULT ==========");
        log.info("Pages: {}", pages.size());

        for (StructuredPage page : pages) {
            log.info("Page {}", page.getPageNumber());
            log.info("OCR: {}", page.isOcr());
            log.info("Blocks: {}", page.getBlocks().size());

            page.getBlocks().forEach(block -> {
                log.info("--------------------------------");
                log.info("Type : {}", block.getType());
                log.info("Text : {}", block.getText());
                log.info("Markdown : {}", block.getMarkdown());
            });
        }
        List<org.springframework.ai.document.Document> splitDocs =
                overlappingTextSplitter.split(pages);
        log.info("========== SPLITTER ==========");
        log.info("Chunks = {}", splitDocs.size());

        splitDocs.forEach(doc -> {
            log.info("-------------------------");
            log.info(doc.getText());
        });
        // Remove duplicate chunks
        Set<String> seenChunks = new HashSet<>();

        splitDocs = splitDocs.stream()
                .filter(doc -> {

                    String text = doc.getText();

                    if (text == null || text.isBlank()) {
                        return false;
                    }

                    return seenChunks.add(text.trim());

                })
                .toList();

        System.out.println("=================================");
        System.out.println("Document: " + document.getOriginalFileName());
        System.out.println("Total chunks: " + splitDocs.size());

        // Add metadata
        for (int i = 0; i < splitDocs.size(); i++) {

            org.springframework.ai.document.Document chunk =
                    splitDocs.get(i);

            chunk.getMetadata().put("documentId", document.getId());

            chunk.getMetadata().put("fileName", document.getOriginalFileName());

            chunk.getMetadata().put(
                    "documentId",
                    document.getId()
            );

            chunk.getMetadata().put("page", chunk.getMetadata().get("page"));

            chunk.getMetadata().put("paragraph", chunk.getMetadata().get("paragraph"));

            chunk.getMetadata().put("heading", chunk.getMetadata().get("heading"));

            chunk.getMetadata().put("chunk", chunk.getMetadata().get("chunk"));

            if (document.getUploadedBy() != null) {
                chunk.getMetadata().put("uploaded_by",
                        document.getUploadedBy().getId());
            }

            if (document.getFileHash() != null) {
                chunk.getMetadata().put("file_hash",
                        document.getFileHash());
            }
        }
        for (var chunk : splitDocs) {

            System.out.println(chunk.getMetadata());

        }
        // Store embeddings
        log.info("===== STEP 1 : OCR + Chunking completed =====");

        log.info("Total chunks = {}", splitDocs.size());

        log.info("===== STEP 2 : Starting vectorStore.accept() =====");

        int batchSize = 3;

        for (int i = 0; i < splitDocs.size(); i += batchSize) {

            int end = Math.min(i + batchSize, splitDocs.size());

            List<org.springframework.ai.document.Document> batch =
                    splitDocs.subList(i, end);

            log.info("Storing batch {} - {}", i, end);

            vectorStore.accept(batch);

            log.info("Stored batch {} - {}", i, end);
        }

        log.info("===== STEP 3 : vectorStore.accept() completed =====");

        log.info("Indexing completed successfully.");
    }
}
