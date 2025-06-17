package com.marcos.studyasistant.documentservice.service.impl;

import com.marcos.studyasistant.documentservice.entity.DocumentEntity;
import com.marcos.studyasistant.documentservice.entity.enums.ProcessingStatus;
import com.marcos.studyasistant.documentservice.exceptions.DocumentNotFoundException;
import com.marcos.studyasistant.documentservice.reposiroty.DocumentsRepository;
import com.marcos.studyasistant.documentservice.service.DocumentsProcessingService;
import com.marcos.studyasistant.documentservice.service.DocumentsStorageService;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class DocumentsProcessingServiceImpl implements DocumentsProcessingService {

    private final DocumentsRepository documentsRepository;
    private final DocumentsStorageService documentsStorageService;

    public DocumentsProcessingServiceImpl(DocumentsRepository documentsRepository,
                                           DocumentsStorageService documentsStorageService) {
        this.documentsRepository = documentsRepository;
        this.documentsStorageService = documentsStorageService;
    }

    @Override
    public CompletableFuture<Void> processDocument(UUID documentId) {
        DocumentEntity document = documentsRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found"));

        try {
            document.setStatus(ProcessingStatus.PROCESSING);
            documentsRepository.save(document);

            String extractedText = extractTextFromDocument(document.getStoragePath());

            document.setExtractedText(extractedText);
            document.setStatus(ProcessingStatus.COMPLETED);
            document.setProcessedAt(LocalDateTime.now());

        } catch (Exception e) {
            document.setStatus(ProcessingStatus.FAILED);
            document.setErrorMessage(e.getMessage());
        } finally {
            documentsRepository.save(document);
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public String extractTextFromDocument(String storagePath) throws Exception {
        try (InputStream inputStream = documentsStorageService.downloadDocument(storagePath)) {
            Tika tika = new Tika();
            return tika.parseToString(inputStream);
        }
    }
}
