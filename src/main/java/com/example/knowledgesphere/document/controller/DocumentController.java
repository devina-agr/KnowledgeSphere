package com.example.knowledgesphere.document.controller;

import com.example.knowledgesphere.document.dto.DocumentDetailsResponse;
import com.example.knowledgesphere.document.dto.DocumentUploadResponse;
import com.example.knowledgesphere.document.service.DocumentService;
import com.example.knowledgesphere.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Validated
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public DocumentUploadResponse upload(

            @RequestPart(value = "file", required = false) MultipartFile filePart,

            @RequestParam(value = "file", required = false) MultipartFile fileParam,

            @AuthenticationPrincipal User user

    ) {

        MultipartFile file = filePart != null ? filePart : fileParam;

        if (file == null || file.isEmpty()) {
            throw new com.example.knowledgesphere.exception.custom.BadRequestException(
                    "File is required."
            );
        }

        return documentService.upload(file, user);

    }

    @GetMapping
    public List<DocumentDetailsResponse> getAll() {

        return documentService.getAll();

    }

}