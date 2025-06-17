package com.marcos.studyasistant.documentservice.mappers;

import com.marcos.studyasistant.documentservice.dto.DocumentResponseDto;
import com.marcos.studyasistant.documentservice.entity.DocumentEntity;
import org.springframework.stereotype.Component;

@Component
public class DocumentMapper {

    public DocumentResponseDto from(DocumentEntity document) {
        return  new DocumentResponseDto(
                document.getId(),
                document.getFilename(),
                document.getOriginalFilename(),
                document.getContentType(),
                document.getFileSize(),
                document.getStatus(),
                document.getUploadedAt(),
                "Admin",
                document.getProcessedAt(),
                document.getErrorMessage());
    }
}
