package com.marcos.studyasistant.documentservice.service;

import com.marcos.studyasistant.documentservice.dto.DocumentContentResponseDto;
import com.marcos.studyasistant.documentservice.dto.DocumentResponseDto;
import com.marcos.studyasistant.documentservice.dto.DocumentUploadRequestDto;

import java.util.UUID;

public interface DocumentsService {

    /**
     * Uploads a document and returns the response containing document details.
     *
     * @param documentUploadRequestDto the request containing the document to be uploaded
     * @return the response containing details of the uploaded document
     */
    DocumentResponseDto uploadDocument(DocumentUploadRequestDto documentUploadRequestDto);

    /**
     * Retrieves a document by its ID.
     *
     * @param id the UUID of the document
     * @return the response containing details of the document
     */
    DocumentResponseDto getDocumentById(UUID id);

    /**
     * Retrieves the content of a document by its ID.
     *
     * @param id the UUID of the document
     * @return the content of the document as a String
     */
    DocumentContentResponseDto getDocumentContent(UUID id);

    /**
     * Deletes a document by its ID.
     *
     * @param id the UUID of the document to be deleted
     */
    void deleteDocument(UUID id);
}
