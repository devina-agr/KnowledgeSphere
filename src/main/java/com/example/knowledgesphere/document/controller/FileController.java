package com.example.knowledgesphere.document.controller;

import com.example.knowledgesphere.entity.Document;
import com.example.knowledgesphere.document.service.DocumentService;
import com.example.knowledgesphere.storage.LocalStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final DocumentService documentService;

    private final LocalStorageService storageService;

    @GetMapping("/{documentId}")

    public ResponseEntity<Resource> openPdf(

            @PathVariable
            Long documentId

    ) {

        Document document =
                documentService.findById(
                        documentId
                );

        Resource resource =
                storageService.load(
                        document.getFilePath()
                );

        return ResponseEntity.ok()

                .contentType(
                        MediaType.APPLICATION_PDF
                )

                .header(

                        HttpHeaders.CONTENT_DISPOSITION,

                        "inline; filename=\"" +

                                document.getOriginalFileName()

                                + "\""

                )

                .body(resource);

    }

}
