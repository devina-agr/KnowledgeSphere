package com.example.knowledgesphere.document.dto;

import com.example.knowledgesphere.entity.DocumentStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentUploadResponse {

    private Long id;

    private String fileName;

    private String url;

    private DocumentStatus status;

    private String message;

    @JsonProperty("documentId")
    public Long getDocumentId() {
        return id;
    }

}