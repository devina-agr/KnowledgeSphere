package com.example.knowledgesphere.repository;

import com.example.knowledgesphere.entity.Document;
import com.example.knowledgesphere.entity.DocumentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByStatus(DocumentStatus status);

    boolean existsByFileHash(String fileHash);

}