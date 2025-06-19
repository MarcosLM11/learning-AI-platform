package com.marcos.studyasistant.documentservice.mappers;

import com.marcos.studyasistant.documentservice.dto.DocumentResponseDto;
import com.marcos.studyasistant.documentservice.entity.DocumentEntity;
import org.springframework.stereotype.Component;

@Component
public class DocumentMapper {

    public DocumentResponseDto from(DocumentEntity document) {
        return  new DocumentResponseDto(
                document.getId(),
                document.getUserId(),
                document.getOriginalFilename(),
                document.getFileSize(),
                document.getMimeType(),
                document.getFilePath(),
                document.getStatus(),
                document.getPageCount(),
                document.getLanguageDetected(),
                document.getProcessingError(),
                document.getCreatedAt(),
                document.getUpdatedAt(),
                document.getProcessedAt(),
                document.getProcessingLogs(),
                document.getTags());
    }
}
