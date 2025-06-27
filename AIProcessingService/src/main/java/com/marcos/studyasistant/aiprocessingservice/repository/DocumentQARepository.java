package com.marcos.studyasistant.aiprocessingservice.repository;

import com.marcos.studyasistant.aiprocessingservice.entity.DocumentQA;
import com.marcos.studyasistant.aiprocessingservice.enums.AIProcessingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentQARepository extends JpaRepository<DocumentQA, UUID> {
    
    Optional<DocumentQA> findByDocumentId(UUID documentId);
    
    List<DocumentQA> findByUserId(UUID userId);
    
    List<DocumentQA> findByStatus(AIProcessingStatus status);
    
    List<DocumentQA> findByUserIdAndStatus(UUID userId, AIProcessingStatus status);
    
    boolean existsByDocumentId(UUID documentId);
}