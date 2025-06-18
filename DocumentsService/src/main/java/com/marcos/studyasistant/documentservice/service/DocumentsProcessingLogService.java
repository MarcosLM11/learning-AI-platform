package com.marcos.studyasistant.documentservice.service;

import com.marcos.studyasistant.documentservice.entity.DocumentEntity;
import com.marcos.studyasistant.documentservice.entity.DocumentProcessingLog;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface DocumentsProcessingLogService {

    /**
     * Logs a processing step for a document.
     *
     * @param document        the document being processed
     * @param processingStep  the name of the processing step
     * @param status          the status of the processing step
     * @param details         additional details about the processing step
     * @return the logged DocumentProcessingLog entity
     */
    DocumentProcessingLog logProcessingStep(DocumentEntity document,
                                            String processingStep,
                                            String status,
                                            Map<String, Object> details);

    /**
     * Logs a processing step for a document with processing time.
     * @param document
     * @param processingStep
     * @param status
     * @param details
     * @param processingTimeMs
     * @return
     */
    DocumentProcessingLog logProcessingStep(DocumentEntity document,
                                            String processingStep,
                                            String status,
                                            Map<String, Object> details,
                                            Long processingTimeMs);

    /**
     * Retrieves the processing history for a specific document.
     *
     * @param documentId the UUID of the document
     * @return a list of DocumentProcessingLog entities representing the processing history
     */
    List<DocumentProcessingLog> getDocumentProcessingHistory(UUID documentId);

    /**
     * Retrieves recent error logs from the processing history.
     *
     * @param hoursBack the number of hours back to look for errors
     * @return a list of DocumentProcessingLog entities representing recent errors
     */
    List<DocumentProcessingLog> getRecentErrors(int hoursBack);
}
