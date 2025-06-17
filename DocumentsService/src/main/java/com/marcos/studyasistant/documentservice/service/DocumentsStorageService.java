package com.marcos.studyasistant.documentservice.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;

public interface DocumentsStorageService {

    /**
     * Uploads a document to the storage.
     *
     * @param file the document file to upload
     * @return the filename of the uploaded document
     */
    String uploadDocument(MultipartFile file) throws Exception;

    /**
     * Downloads a document from the storage.
     *
     * @param filename the name of the document file to download
     * @return an InputStream to read the document content
     */
    InputStream downloadDocument(String filename) throws Exception;

    /**
     * Deletes a document from the storage.
     *
     * @param filename the name of the document file to delete
     */
    void deleteDocument(String filename) throws Exception;

    /**
     * Creates a storage bucket if it does not already exist.
     */
    void createBucketIfNotExists();
}
