package com.example.knowledgesphere.audit.repository;

import com.example.knowledgesphere.audit.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditRepository extends JpaRepository<AuditLog, Long> {

}