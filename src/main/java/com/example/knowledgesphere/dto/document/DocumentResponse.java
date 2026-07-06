package com.example.knowledgesphere.dto.document;

import com.example.knowledgesphere.entity.DocumentStatus;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponse {

    private Long id;

    private String fileName;

    private String originalFileName;

    private DocumentStatus status;

}