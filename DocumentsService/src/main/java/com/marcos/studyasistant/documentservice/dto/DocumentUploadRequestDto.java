package com.marcos.studyasistant.documentservice.dto;

import org.springframework.web.multipart.MultipartFile;

public record DocumentUploadRequestDto(
        MultipartFile file
) {}
