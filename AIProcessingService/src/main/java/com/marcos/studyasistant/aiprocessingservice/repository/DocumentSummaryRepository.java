package com.marcos.studyasistant.aiprocessingservice.repository;

import com.marcos.studyasistant.aiprocessingservice.entity.DocumentSummary;
import com.marcos.studyasistant.aiprocessingservice.enums.AIProcessingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentSummaryRepository extends JpaRepository<DocumentSummary, UUID> {
    
    Optional<DocumentSummary> findByDocumentId(UUID documentId);
    
    List<DocumentSummary> findByUserId(UUID userId);
    
    List<DocumentSummary> findByStatus(AIProcessingStatus status);
    
    List<DocumentSummary> findByUserIdAndStatus(UUID userId, AIProcessingStatus status);
    
    boolean existsByDocumentId(UUID documentId);
}