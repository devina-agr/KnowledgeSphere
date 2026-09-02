package com.example.knowledgesphere.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentBlock {

    private BlockType type;

    /**
     * Markdown representation.
     */
    private String markdown;

    /**
     * Optional HTML.
     * Mainly useful for complex tables.
     */
    private String html;

    /**
     * Original extracted text.
     */
    private String text;

    private Map<String,Object> metadata = new HashMap<>();

}
