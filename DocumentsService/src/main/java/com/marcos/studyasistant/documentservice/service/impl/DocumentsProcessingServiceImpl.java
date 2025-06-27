package com.marcos.studyasistant.documentservice.service.impl;

import com.marcos.studyasistant.documentservice.dto.PageCountResultDto;
import com.marcos.studyasistant.documentservice.entity.DocumentEntity;
import com.marcos.studyasistant.documentservice.dto.LanguageDetectionResultDto;
import com.marcos.studyasistant.documentservice.entity.enums.ProcessingStatus;
import com.marcos.studyasistant.documentservice.exceptions.DocumentNotFoundException;
import com.marcos.studyasistant.documentservice.exceptions.DocumentProcessingException;
import com.marcos.studyasistant.documentservice.reposiroty.DocumentsRepository;
import com.marcos.studyasistant.documentservice.service.*;
import com.marcos.studyasistant.documentservice.utils.HashUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.google.common.io.Files.getFileExtension;

@Service
@Slf4j
public class DocumentsProcessingServiceImpl implements DocumentsProcessingService {

    private final DocumentsRepository documentsRepository;
    private final DocumentsStorageService documentsStorageService;
    private final DocumentsProcessingLogService documentsProcessingLogService;
    private final DocumentTagService documentTagService;
    private final LanguageDetectionService languageDetectionService;
    private final PageCountService pageCountService;
    private final HashUtil hashUtil;
    private final DocumentEventPublisher documentEventPublisher;

    public DocumentsProcessingServiceImpl(DocumentsRepository documentsRepository,
                                           DocumentsStorageService documentsStorageService,
                                          DocumentsProcessingLogService documentsProcessingLogService,
                                          DocumentTagService documentTagService,
                                          LanguageDetectionService languageDetectionService,
                                          PageCountService pageCountService,
                                          HashUtil hashUtil,
                                          DocumentEventPublisher documentEventPublisher) {
        this.documentsRepository = documentsRepository;
        this.documentsStorageService = documentsStorageService;
        this.documentsProcessingLogService = documentsProcessingLogService;
        this.documentTagService = documentTagService;
        this.languageDetectionService = languageDetectionService;
        this.pageCountService = pageCountService;
        this.hashUtil = hashUtil;
        this.documentEventPublisher = documentEventPublisher;
    }

    @Override
    public CompletableFuture<Void> processDocument(UUID documentId) {
        long startTime = System.currentTimeMillis();

        DocumentEntity document = documentsRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found"));

        File tempFile = null;

        try {
            log.info("Starting processing for document: {}", document.getId());

            // Log the start of the processing step
            documentsProcessingLogService.logProcessingStep(document, "PROCESSING_STARTED", "SUCCESS",
                    Map.of("originalFilename", document.getOriginalFilename()));

            tempFile = downloadToTempFile(document.getFilePath());

            log.info("Downloading document from Minio: {}", document.getFilePath());

            // Update document status to PROCESSING
            document.setStatus(ProcessingStatus.PROCESSING);
            documentsRepository.save(document);

            // Extract text from the document
            long extractionStartTime = System.currentTimeMillis();
            String extractedText = extractTextFromDocument(tempFile);
            long extractionTime = System.currentTimeMillis() - extractionStartTime;

            log.info("Text extracted from document {}: {} characters", document.getId(), extractedText.length());

            // Log the text extraction step
            document.setExtractedText(extractedText);
            documentsProcessingLogService.logProcessingStep(document, "TEXT_EXTRACTION", "SUCCESS",
                    Map.of("textLength", extractedText.length()), extractionTime);

            // hash the extracted text + original filename to prevent duplicates
            String hash = document.getOriginalFilename() + extractedText;
            String documentHash = hashUtil.generateSHA256Hash(hash);
            document.setHash(documentHash);

            documentsProcessingLogService.logProcessingStep(document, "DOCUMENT_HASH", "SUCCESS",
                    Map.of("textLength", extractedText.length()), extractionTime);


            // Detect language
            long languageStart = System.currentTimeMillis();
            String language = detectDocumentLanguage(document, extractedText);
            long languageTime = System.currentTimeMillis() - languageStart;

            log.info("Language detected for document {}: {}", document.getId(), language);

            // Log the language detection step
            document.setLanguageDetected(language);
            documentsProcessingLogService.logProcessingStep(document, "LANGUAGE_DETECTION", "SUCCESS",
                    Map.of("detectedLanguage", language), languageTime);

            // Count pages in the document
            long pageCountStart = System.currentTimeMillis();
            Integer pageCount = countPages(document, tempFile);
            long pageCountTime = System.currentTimeMillis() - pageCountStart;

            // Log the page count step
            document.setPageCount(pageCount);
            documentsProcessingLogService.logProcessingStep(document, "PAGE_COUNT", "SUCCESS",
                    Map.of("pageCount", pageCount), pageCountTime);

            // Generar tags automáticos
            long taggingStart = System.currentTimeMillis();
            Map<String, BigDecimal> autoTags = generateAutomaticTags(extractedText, document.getMimeType());
            documentTagService.addAutoGeneratedTags(document, autoTags);
            long taggingTime = System.currentTimeMillis() - taggingStart;

            // Log the auto-tagging step
            documentsProcessingLogService.logProcessingStep(document, "AUTO_TAGGING", "SUCCESS",
                    Map.of("tagsGenerated", autoTags.size()), taggingTime);

            // Finalizar procesamiento
            document.setStatus(ProcessingStatus.COMPLETED);
            document.setProcessedAt(LocalDateTime.now());
            documentsRepository.save(document);

            log.info("Document {} processed successfully", document.getId());

            // Log the completion of the processing step
            long totalTime = System.currentTimeMillis() - startTime;
            documentsProcessingLogService.logProcessingStep(document, "PROCESSING_COMPLETED", "SUCCESS",
                    Map.of("totalProcessingTimeMs", totalTime), totalTime);

            // Publish event for AI processing
            documentEventPublisher.publishDocumentProcessingCompleted(document);


        } catch (Exception e) {
            handleProcessingError(document, e, System.currentTimeMillis() - startTime);
        } finally {
            if (tempFile != null && tempFile.exists()) {
                if (!tempFile.delete()) {
                    log.warn("Failed to delete temporary file: {}", tempFile.getAbsolutePath());
                }
                log.info("Deleting temporary file: {}", tempFile.getAbsolutePath());
            }
        }

        return CompletableFuture.completedFuture(null);
    }

    private void handleProcessingError(DocumentEntity document, Exception e, long processingTime) {
        document.setStatus(ProcessingStatus.FAILED);
        document.setProcessingError(e.getMessage());
        documentsRepository.save(document);

        documentsProcessingLogService.logProcessingStep(document, "PROCESSING_FAILED", "ERROR",
                Map.of(
                        "errorMessage", e.getMessage(),
                        "errorClass", e.getClass().getSimpleName(),
                        "stackTrace", Arrays.toString(e.getStackTrace()).substring(0, Math.min(1000, Arrays.toString(e.getStackTrace()).length()))
                ), processingTime);

        log.error("Error processing document {}: {}", document.getId(), e.getMessage(), e);
    }

    private Map<String, BigDecimal> generateAutomaticTags(String text, String mimeType) {
        Map<String, BigDecimal> tags = new HashMap<>();

        // Tags basados en tipo de archivo
        if (mimeType.contains("pdf")) {
            tags.put("pdf", new BigDecimal("1.00"));
        } else if (mimeType.contains("word")) {
            tags.put("document", new BigDecimal("1.00"));
        }

        // Tags basados en contenido (ejemplo básico)
        String lowerText = text.toLowerCase();
        if (lowerText.contains("contract") || lowerText.contains("agreement")) {
            tags.put("contract", new BigDecimal("0.85"));
        }
        if (lowerText.contains("invoice") || lowerText.contains("bill")) {
            tags.put("financial", new BigDecimal("0.90"));
        }
        if (lowerText.contains("report") || lowerText.contains("analysis")) {
            tags.put("report", new BigDecimal("0.80"));
        }

        log.info("Generated {} automatic tags for document: {}", tags.size(), text.length() > 50 ? text.substring(0, 50) + "..." : text);

        return tags;
    }

    private String extractTextFromDocument(File tempFile) throws Exception {
        Tika tika = new Tika();
        FileInputStream inputStream = new FileInputStream(tempFile);
        return tika.parseToString(inputStream);
    }

    private String detectDocumentLanguage(DocumentEntity document, String extractedText) {
        try {
            LanguageDetectionResultDto result = languageDetectionService.detectLanguage(extractedText);

            Map<String, Object> logDetails = new HashMap<>();
            logDetails.put("detectedLanguage", result.getLanguage());
            logDetails.put("confidence", result.getConfidence());
            logDetails.put("reliable", result.isReliable());
            logDetails.put("textLength", extractedText.length());

            if (result.getReason() != null) {
                logDetails.put("reason", result.getReason());
            }

            String logStatus = "SUCCESS";
            if ("unknown".equals(result.getLanguage())) {
                logStatus = "WARNING";
                log.warn("Could not detect language for document {}: {}",
                        document.getId(), result.getReason());
            } else if (!result.isReliable()) {
                logStatus = "WARNING";
                log.warn("Low confidence language detection for document {}: {} ({})",
                        document.getId(), result.getLanguage(), result.getConfidence());
            } else {
                log.info("Language detected for document {}: {} (confidence: {})",
                        document.getId(), result.getLanguage(), result.getConfidence());
            }

            documentsProcessingLogService.logProcessingStep(document, "LANGUAGE_DETECTION", logStatus, logDetails);
            return result.getLanguage();

        } catch (Exception e) {
            log.error("Error detecting language for document {}: {}", document.getId(), e.getMessage(), e);

            documentsProcessingLogService.logProcessingStep(document, "LANGUAGE_DETECTION", "ERROR",
                    Map.of("errorMessage", e.getMessage()));

            return "unknown";
        }
    }

    private Integer countPages(DocumentEntity document, File tempFile) {
        try {
            FileInputStream inputStream = new FileInputStream(tempFile);
            PageCountResultDto pageCount = pageCountService.countPagesDetailed(document, inputStream);

            if (pageCount != null) {
                log.info("Page count for document {}: {} pages ({})",
                        document.getId(), pageCount.getPageCount(), document.getMimeType());
            } else {
                log.info("Could not count pages for document {}: unsupported type {}",
                        document.getId(), document.getMimeType());
            }

            return pageCount.getPageCount();

        } catch (Exception e) {
            log.error("Error counting pages for document {}: {}", document.getId(), e.getMessage(), e);
            return null;
        }
    }

    private File downloadToTempFile(String filePath) throws DocumentProcessingException {
        try {
            // Crear archivo temporal con extensión apropiada
            File tempFile = Files.createTempFile("document_", getFileExtension(filePath)).toFile();

            // Descargar de Minio al archivo temporal
            try (InputStream minioStream = documentsStorageService.downloadDocument(filePath);
                 FileOutputStream fos = new FileOutputStream(tempFile)) {

                // Copiar contenido
                minioStream.transferTo(fos);
            }

            return tempFile;
        } catch (Exception e) {
            throw new DocumentProcessingException("Failed to download to temp file :" + e);
        }
    }
}
