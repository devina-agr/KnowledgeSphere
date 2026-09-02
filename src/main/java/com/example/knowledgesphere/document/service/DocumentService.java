package com.example.knowledgesphere.document.service;

import com.example.knowledgesphere.ai.service.AIEmbeddingService;
import com.example.knowledgesphere.document.dto.DocumentDetailsResponse;
import com.example.knowledgesphere.document.dto.DocumentUploadResponse;
import com.example.knowledgesphere.document.mapper.DocumentMapper;
import com.example.knowledgesphere.entity.Document;
import com.example.knowledgesphere.entity.DocumentStatus;
import com.example.knowledgesphere.entity.User;
import com.example.knowledgesphere.exception.custom.ResourceNotFoundException;
import com.example.knowledgesphere.repository.DocumentRepository;
import com.example.knowledgesphere.storage.LocalStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final DocumentRepository repository;

    private final DocumentMapper mapper;

    private final AIEmbeddingService embeddingService;

    private final FileHashService fileHashService;

    private final LocalStorageService localStorageService;

    public List<DocumentDetailsResponse> getAll(){

        return repository.findAll()

                .stream()

                .map(mapper::toDetails)

                .toList();

    }

    public Document findById(Long id){

        return repository.findById(id)

                .orElseThrow(

                        ()->new ResourceNotFoundException(

                                "Document not found."

                        )

                );

    }
    
    public DocumentUploadResponse upload(
            MultipartFile file,
            User uploadedBy
    ) {

        String hash = fileHashService.calculateHash(file);

        if (repository.existsByFileHash(hash)) {
            throw new RuntimeException("This document has already been uploaded.");
        }

        String filePath =
                localStorageService.save(file);

        Document document = Document.builder()

                .fileName(file.getOriginalFilename())

                .originalFileName(file.getOriginalFilename())

                .fileHash(hash)

                .contentType(file.getContentType())

                .fileSize(file.getSize())

                .filePath(filePath)

                .uploadedBy(uploadedBy)

                .status(DocumentStatus.PROCESSING)

                .build();

        repository.save(document);
        repository.flush();

        try {
            byte[] fileBytes = file.getBytes();
            embeddingService.indexDocument(document, fileBytes);
            document.setStatus(DocumentStatus.COMPLETED);
        } catch (Exception e) {

            log.error("Failed to index uploaded document {}", document.getOriginalFileName(), e);

            document.setStatus(DocumentStatus.FAILED);

            e.printStackTrace();

            throw new RuntimeException(e);

        }

        repository.save(document);

        return mapper.toUploadResponse(document);

    }
}