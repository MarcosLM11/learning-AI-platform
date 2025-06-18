package com.marcos.studyasistant.documentservice.service;

import com.marcos.studyasistant.documentservice.entity.LanguageDetectionResult;

public interface LanguageDetectionService {

    LanguageDetectionResult detectLanguage(String text);
}
