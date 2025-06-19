package com.marcos.studyasistant.documentservice.service;

import com.marcos.studyasistant.documentservice.dto.LanguageDetectionResultDto;

public interface LanguageDetectionService {

    LanguageDetectionResultDto detectLanguage(String text);
}
