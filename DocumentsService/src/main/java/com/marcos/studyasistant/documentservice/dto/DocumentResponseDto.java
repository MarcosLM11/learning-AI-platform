package com.marcos.studyasistant.documentservice.dto;

import com.marcos.studyasistant.documentservice.entity.enums.ProcessingStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record DocumentResponseDto (
        UUID id,
        String fileName,
        String originalFileName,
        String contentType,
        Long size,
        ProcessingStatus processingStatus,
        LocalDateTime uploadedAt,
        String uploadedBy,
        LocalDateTime processedAt,
        String errorMessage
) {}
