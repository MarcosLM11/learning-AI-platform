package com.marcos.studyasistant.documentservice.service.impl;

import com.marcos.studyasistant.documentservice.entity.DocumentEntity;
import com.marcos.studyasistant.documentservice.entity.DocumentProcessingLog;
import com.marcos.studyasistant.documentservice.reposiroty.DocumentProcessingLogRepository;
import com.marcos.studyasistant.documentservice.service.DocumentsProcessingLogService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class DocumentsProcessingLogServiceImpl implements DocumentsProcessingLogService {

    private final DocumentProcessingLogRepository logRepository;

    public DocumentsProcessingLogServiceImpl(DocumentProcessingLogRepository logRepository) {
        this.logRepository = logRepository;
    }

    @Override
    public DocumentProcessingLog logProcessingStep(DocumentEntity document,
                                                   String processingStep,
                                                   String status,
                                                   Map<String, Object> details) {
        return logProcessingStep(document, processingStep, status, details, null);
    }

    @Override
    public DocumentProcessingLog logProcessingStep(DocumentEntity document,
                                                   String processingStep,
                                                   String status,
                                                   Map<String, Object> details,
                                                   Long processingTimeMs) {
        DocumentProcessingLog log = new DocumentProcessingLog();
        log.setDocument(document);
        log.setProcessingStep(processingStep);
        log.setStatus(status);
        log.setDetails(details != null ? details : new HashMap<>());
        log.setProcessingTimeMs(processingTimeMs != null ? processingTimeMs.intValue() : null);

        return logRepository.save(log);
    }

    @Override
    public List<DocumentProcessingLog> getDocumentProcessingHistory(UUID documentId) {
        return logRepository.findByDocumentIdOrderByCreatedAtDesc(documentId);
    }

    @Override
    public List<DocumentProcessingLog> getRecentErrors(int hoursBack) {
        LocalDateTime since = LocalDateTime.now().minusHours(hoursBack);
        return logRepository.findErrorsSince(since);
    }
}
