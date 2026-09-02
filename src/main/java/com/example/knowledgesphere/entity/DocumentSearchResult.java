package com.example.knowledgesphere.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DocumentSearchResult {

    private String content;

    private String fileName;

    private Integer page;

    private Integer paragraph;

    private Integer chunk;

    private String heading;

    private Long documentId;

    private String pdfUrl;
}
