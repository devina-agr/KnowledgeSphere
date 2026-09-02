package com.example.knowledgesphere.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StructuredPage {

    private int pageNumber;

    @Builder.Default
    private boolean ocr = false;

    @Builder.Default
    private List<DocumentBlock> blocks = new ArrayList<>();

    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    /**
     * Returns all searchable text from the page.
     */
    public String searchableText() {

        StringBuilder builder = new StringBuilder();

        if (blocks != null) {

            for (DocumentBlock block : blocks) {

                if (block.getText() != null && !block.getText().isBlank()) {
                    builder.append(block.getText()).append("\n");
                }

                if (block.getMarkdown() != null && !block.getMarkdown().isBlank()) {
                    builder.append(block.getMarkdown()).append("\n");
                }
            }
        }

        return builder.toString().trim();
    }

    /**
     * Returns all table blocks.
     */
    public List<DocumentBlock> getTables() {

        if (blocks == null) {
            return Collections.emptyList();
        }

        return blocks.stream()
                .filter(block -> block.getType() == BlockType.TABLE)
                .toList();
    }

    /**
     * Returns all image and caption blocks.
     */
    public List<DocumentBlock> getImageCaptions() {

        if (blocks == null) {
            return Collections.emptyList();
        }

        return blocks.stream()
                .filter(block ->
                        block.getType() == BlockType.IMAGE ||
                                block.getType() == BlockType.CAPTION)
                .toList();
    }
}