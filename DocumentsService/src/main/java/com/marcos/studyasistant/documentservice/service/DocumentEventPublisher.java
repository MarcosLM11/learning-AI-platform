package com.marcos.studyasistant.documentservice.service;

import com.marcos.studyasistant.documentservice.entity.DocumentEntity;

public interface DocumentEventPublisher {
    
    void publishDocumentProcessingCompleted(DocumentEntity document);
}