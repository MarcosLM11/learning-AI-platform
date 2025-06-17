package com.marcos.studyasistant.documentservice.controller;

import com.marcos.studyasistant.documentservice.dto.DocumentContentResponseDto;
import com.marcos.studyasistant.documentservice.dto.DocumentResponseDto;
import com.marcos.studyasistant.documentservice.dto.DocumentUploadRequestDto;
import com.marcos.studyasistant.documentservice.service.DocumentsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
public class DocumentsController {

    private final DocumentsService documentsService;

    public DocumentsController(DocumentsService documentsService) {
        this.documentsService = documentsService;
    }

    @PostMapping("/upload")
    public ResponseEntity<DocumentResponseDto> uploadDocument(
            @RequestBody DocumentUploadRequestDto documentUploadRequestDto) {

        DocumentResponseDto response = documentsService.uploadDocument(documentUploadRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponseDto> getDocument(@PathVariable UUID id) {
        DocumentResponseDto document = documentsService.getDocumentById(id);
        return ResponseEntity.ok(document);
    }

    @GetMapping("/{id}/content")
    public ResponseEntity<DocumentContentResponseDto> getDocumentContent(@PathVariable UUID id) {
        DocumentContentResponseDto documentContentResponseDto = documentsService.getDocumentContent(id);
        return ResponseEntity.ok(documentContentResponseDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@RequestParam UUID id) {
        documentsService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }


}
