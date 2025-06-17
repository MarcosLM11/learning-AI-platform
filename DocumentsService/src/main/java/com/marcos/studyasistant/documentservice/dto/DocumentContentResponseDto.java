package com.marcos.studyasistant.documentservice.dto;

import com.marcos.studyasistant.documentservice.entity.enums.ProcessingStatus;
import java.util.UUID;

public record DocumentContentResponseDto(
        UUID id,
        String filename,
        String extractedText,
        ProcessingStatus status
) {
}
