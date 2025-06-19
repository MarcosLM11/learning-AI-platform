package com.marcos.studyasistant.documentservice.dto;

import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public record DocumentUploadRequestDto(
        UUID userId,
        MultipartFile file
) {}
