package com.example.knowledgesphere.document.mapper;

import com.example.knowledgesphere.document.dto.DocumentDetailsResponse;
import com.example.knowledgesphere.document.dto.DocumentUploadResponse;
import com.example.knowledgesphere.entity.Document;
import com.example.knowledgesphere.entity.DocumentStatus;
import org.springframework.stereotype.Component;

@Component
public class DocumentMapper {

    public DocumentUploadResponse toUploadResponse(
            Document document
    ) {

        String message = document.getStatus() == DocumentStatus.COMPLETED
                ? "Document uploaded and indexed successfully."
                : "Document uploaded but indexing failed.";

        return DocumentUploadResponse.builder()

                .id(document.getId())

                .fileName(document.getFileName())

                // URL to your FileController
                .url("/api/files/" + document.getId())

                .status(document.getStatus())

                .message(message)

                .build();

    }

    public DocumentDetailsResponse toDetails(
            Document document
    ) {

        return DocumentDetailsResponse.builder()

                .id(document.getId())

                .fileName(document.getFileName())

                .originalFileName(document.getOriginalFileName())

                .contentType(document.getContentType())

                .fileSize(document.getFileSize())

                .url("/api/files/" + document.getId())

                .status(document.getStatus())

                .uploadedAt(document.getUploadedAt())

                .build();

    }

}