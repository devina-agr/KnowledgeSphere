package com.example.knowledgesphere.dto.document;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadResponse {

    private String message;

    private Long documentId;

}