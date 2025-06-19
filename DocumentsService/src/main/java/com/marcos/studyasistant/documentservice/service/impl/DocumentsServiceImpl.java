package com.marcos.studyasistant.documentservice.service.impl;

import com.marcos.studyasistant.documentservice.dto.DocumentContentResponseDto;
import com.marcos.studyasistant.documentservice.dto.DocumentResponseDto;
import com.marcos.studyasistant.documentservice.dto.DocumentUploadRequestDto;
import com.marcos.studyasistant.documentservice.entity.DocumentEntity;
import com.marcos.studyasistant.documentservice.entity.enums.ProcessingStatus;
import com.marcos.studyasistant.documentservice.exceptions.DocumentNotFoundException;
import com.marcos.studyasistant.documentservice.exceptions.DocumentProcessingException;
import com.marcos.studyasistant.documentservice.mappers.DocumentMapper;
import com.marcos.studyasistant.documentservice.reposiroty.DocumentsRepository;
import com.marcos.studyasistant.documentservice.service.DocumentsProcessingService;
import com.marcos.studyasistant.documentservice.service.DocumentsService;
import com.marcos.studyasistant.documentservice.service.DocumentsStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class DocumentsServiceImpl implements DocumentsService {

    private final DocumentsRepository documentsRepository;
    private final DocumentsStorageService documentsStorageService;
    private final DocumentsProcessingService documentsProcessingService;
    private final DocumentMapper documentMapper;

    public DocumentsServiceImpl(DocumentsRepository documentsRepository,
                                DocumentsStorageService documentsStorageService,
                                DocumentsProcessingService documentsProcessingService,
                                DocumentMapper documentMapper) {
        this.documentsRepository = documentsRepository;
        this.documentsStorageService = documentsStorageService;
        this.documentsProcessingService = documentsProcessingService;
        this.documentMapper = documentMapper;
    }

    @Override
    public DocumentResponseDto uploadDocument(DocumentUploadRequestDto documentUploadRequestDto) {
        MultipartFile file = documentUploadRequestDto.file();
        validate(file);

        try {
            String storagePath = documentsStorageService.uploadDocument(file);

            DocumentEntity document = DocumentEntity.builder()
                    .userId(documentUploadRequestDto.userId())
                    .originalFilename(file.getOriginalFilename())
                    .filePath(storagePath)
                    .mimeType(file.getContentType())
                    .fileSize(file.getSize())
                    .status(ProcessingStatus.UPLOADED)
                    .build();

            document = documentsRepository.save(document);

            // Process the document asynchronously
            documentsProcessingService.processDocument(document.getId());

            return documentMapper.from(document);
        } catch (Exception e) {
            throw new DocumentProcessingException("Failed to upload document : " + e.getMessage());
        }

    }

    @Override
    public DocumentResponseDto getDocumentById(UUID id) {
        DocumentEntity document = documentsRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found"));
        return documentMapper.from(document);
    }

    @Override
    public DocumentContentResponseDto getDocumentContent(UUID id) {
        DocumentEntity document = documentsRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found"));

        return new DocumentContentResponseDto(
                document.getId(),
                document.getOriginalFilename(),
                document.getExtractedText(),
                document.getStatus()
        );
    }

    @Override
    public void deleteDocument(UUID id) {
        DocumentEntity document = documentsRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found"));

        try {
            documentsStorageService.deleteDocument(document.getFilePath());
            documentsRepository.delete(document);
        } catch (Exception e) {
            throw new DocumentProcessingException("Failed to delete document : " + e.getMessage());
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo no puede estar vacío");
        }
    }
}
