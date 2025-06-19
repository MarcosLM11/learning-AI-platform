package com.marcos.studyasistant.documentservice.dto;

import com.marcos.studyasistant.documentservice.entity.DocumentProcessingLog;
import com.marcos.studyasistant.documentservice.entity.DocumentTag;
import com.marcos.studyasistant.documentservice.entity.enums.ProcessingStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record DocumentResponseDto (
        UUID id,
        UUID userId,
        String originalFileName,
        Long fileSize,
        String mimeType,
        String filePath,
        ProcessingStatus status,
        Integer pageCount,
        String languageDetected,
        String processingError,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime processedAt,
        List<DocumentProcessingLog> processingLogs,
        List<DocumentTag> tags
) {}
