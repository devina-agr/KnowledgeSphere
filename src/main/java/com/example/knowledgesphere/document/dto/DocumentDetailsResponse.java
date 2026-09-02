package com.example.knowledgesphere.document.dto;

import com.example.knowledgesphere.entity.DocumentStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDetailsResponse {

    private Long id;

    private String fileName;

    private String originalFileName;

    private String contentType;

    private Long fileSize;

    private String url;

    private DocumentStatus status;

    private LocalDateTime uploadedAt;

}