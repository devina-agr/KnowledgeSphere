package com.example.knowledgesphere.document.service;

import com.example.knowledgesphere.entity.RetrievedDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentRetrievalService {

    private final VectorStore vectorStore;

    public List<RetrievedDocument> retrieve(String question) {

        log.info("Searching vector store for : {}", question);

        List<Document> docs =
                vectorStore.similaritySearch(

                        SearchRequest.builder()

                                .query(question)

                                .topK(10)

                                .similarityThreshold(0.45)

                                .build()

                );

        if (docs == null || docs.isEmpty()) {

            log.info("No matching chunks found.");

            return List.of();

        }

        List<RetrievedDocument> results =
                new ArrayList<>();

        for (Document doc : docs) {

            Map<String, Object> metadata =
                    doc.getMetadata();

            RetrievedDocument result =
                    RetrievedDocument.builder()

                            .content(doc.getText())

                            .fileName(
                                    getString(metadata, "fileName")
                            )

                            .page(
                                    getInteger(metadata, "page")
                            )

                            .paragraph(
                                    getInteger(metadata, "paragraph")
                            )

                            .chunk(
                                    getInteger(metadata, "chunk")
                            )

                            .heading(
                                    getString(metadata, "heading")
                            )

                            .documentId(
                                    getLong(metadata, "documentId")
                            )

                            .pdfUrl(
                                    getString(metadata, "pdfUrl")
                            )
                            .pdfUrl(
                                    getString(
                                            metadata,
                                            "pdfUrl"
                                    )
                            )
                            .build();

            results.add(result);

        }

        log.info("Retrieved {} chunks", results.size());

        for (RetrievedDocument doc : results) {

            log.info("------------------------------------");

            log.info("File      : {}", doc.getFileName());

            log.info("Page      : {}", doc.getPage());

            log.info("Paragraph : {}", doc.getParagraph());

            log.info("Heading   : {}", doc.getHeading());

            log.info("Chunk     : {}", doc.getChunk());

        }

        return results;

    }

    private String getString(
            Map<String, Object> metadata,
            String key
    ) {

        Object value = metadata.get(key);

        return value == null ? null : value.toString();

    }

    private Integer getInteger(
            Map<String, Object> metadata,
            String key
    ) {

        Object value = metadata.get(key);

        if (value == null)
            return null;

        return Integer.parseInt(value.toString());

    }

    private Long getLong(
            Map<String, Object> metadata,
            String key
    ) {

        Object value = metadata.get(key);

        if (value == null)
            return null;

        return Long.parseLong(value.toString());

    }

}
