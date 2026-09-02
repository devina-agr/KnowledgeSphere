package com.example.knowledgesphere.ai.tool;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class DocumentSearchTool {

    private final VectorStore vectorStore;

    @Tool(description = "Search uploaded documents")

    public String search(String query) {

        log.info("==== DOCUMENT SEARCH TOOL CALLED ====");
        log.info("Query: {}", query);

        List<Document> docs =
                vectorStore.similaritySearch(

                        SearchRequest.builder()
                                .query(query)
                                .topK(8)
                                .similarityThreshold(0.45)
                                .build()

                );

        log.info("Retrieved {} chunks", docs.size());

        if (docs == null || docs.isEmpty())
            return "No relevant document found.";

        return docs.stream()

                .map(doc -> {

                    var m = doc.getMetadata();

                    return """

File : %s

Page : %s

Paragraph : %s

Heading : %s

Content :

%s

""".formatted(

                            m.getOrDefault("fileName", "Unknown"),

                            m.getOrDefault("page", "-"),

                            m.getOrDefault("paragraph", "-"),

                            m.getOrDefault("heading", "-"),

                            doc.getText()

                    );

                })

                .collect(Collectors.joining(
                        "\n------------------------\n"
                ));
    }
}