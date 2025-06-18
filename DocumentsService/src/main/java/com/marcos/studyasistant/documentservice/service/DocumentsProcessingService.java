package com.marcos.studyasistant.documentservice.service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface DocumentsProcessingService {

    /**
     * Processes a document asynchronously.
     *
     * @param documentId the UUID of the document to be processed
     * @return a CompletableFuture that will complete when the processing is done
     */
    CompletableFuture<Void> processDocument(UUID documentId);

}
