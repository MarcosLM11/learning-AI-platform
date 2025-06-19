package com.marcos.studyasistant.documentservice.service;

import com.marcos.studyasistant.documentservice.dto.PageCountResultDto;
import com.marcos.studyasistant.documentservice.entity.DocumentEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class PageCountService {

    public PageCountResultDto countPagesDetailed(DocumentEntity document, InputStream inputStream) {
        if (document == null || document.getFilePath() == null) {
            return PageCountResultDto.unsupported("unknown");
        }

        try {
            String mimeType = document.getMimeType().toLowerCase();

            return switch (mimeType) {
                case "application/pdf" -> {
                    Integer count = countPdfPages(inputStream);
                    yield PageCountResultDto.exact(count, "PDF");
                }
                case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> {
                    Integer count = countDocxPages(inputStream);
                    yield PageCountResultDto.estimated(count, "Word DOCX", "Based on content analysis");
                }
                case "application/vnd.openxmlformats-officedocument.presentationml.presentation" -> {
                    Integer count = countPptxPages(inputStream);
                    yield PageCountResultDto.exact(count, "PowerPoint PPTX");
                }
                case "text/plain" -> {
                    Integer count = countTextPages(inputStream, document.getFileSize());
                    yield PageCountResultDto.estimated(count, "Text", "~3000 chars per page");
                }
                default -> PageCountResultDto.unsupported(mimeType);
            };

        } catch (Exception e) {
            log.error("Error counting pages: {}", e.getMessage(), e);
            return PageCountResultDto.unsupported(document.getMimeType());
        }
    }

    private Integer countPdfPages(InputStream inputStream) throws IOException {
        try (PDDocument document = PDDocument.load(inputStream)) {
            int pageCount = document.getNumberOfPages();
            log.debug("PDF pages counted: {}", pageCount);
            return pageCount;
        }
    }

    private Integer countDocxPages(InputStream inputStream) throws IOException {
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            // Word no tiene un método directo para contar páginas
            // Estimamos basándonos en el contenido
            int pageCount = estimateWordPages(document);
            log.debug("DOCX pages estimated: {}", pageCount);
            return pageCount;
        }
    }

    private Integer countDocPages(InputStream inputStream) throws IOException {
        try (HWPFDocument document = new HWPFDocument(inputStream)) {
            // Estimación basada en el contenido
            Range range = document.getRange();
            int pageCount = estimateDocPages(range);
            log.debug("DOC pages estimated: {}", pageCount);
            return pageCount;
        }
    }

    private Integer countPptxPages(InputStream inputStream) throws IOException {
        try (XMLSlideShow presentation = new XMLSlideShow(inputStream)) {
            int slideCount = presentation.getSlides().size();
            log.debug("PPTX slides counted: {}", slideCount);
            return slideCount;
        }
    }

    private Integer countPptPages(InputStream inputStream) throws IOException {
        try (HSLFSlideShow presentation = new HSLFSlideShow(inputStream)) {
            int slideCount = presentation.getSlides().size();
            log.debug("PPT slides counted: {}", slideCount);
            return slideCount;
        }
    }

    private Integer countXlsxPages(InputStream inputStream) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            int sheetCount = workbook.getNumberOfSheets();
            log.debug("XLSX sheets counted: {}", sheetCount);
            return sheetCount;
        }
    }

    private Integer countXlsPages(InputStream inputStream) throws IOException {
        try (HSSFWorkbook workbook = new HSSFWorkbook(inputStream)) {
            int sheetCount = workbook.getNumberOfSheets();
            log.debug("XLS sheets counted: {}", sheetCount);
            return sheetCount;
        }
    }

    private Integer countTextPages(InputStream inputStream, Long fileSize) throws IOException {
        final int CHARS_PER_PAGE = 3000;

        if (fileSize != null && fileSize > 0) {
            int estimatedPages = Math.max(1, (int) Math.ceil(fileSize.doubleValue() / CHARS_PER_PAGE));
            log.debug("Text pages estimated based on file size: {}", estimatedPages);
            return estimatedPages;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            long charCount = reader.lines().mapToLong(String::length).sum();
            int estimatedPages = Math.max(1, (int) Math.ceil(charCount / (double) CHARS_PER_PAGE));
            log.debug("Text pages estimated based on content: {}", estimatedPages);
            return estimatedPages;
        }
    }

    private Integer estimateWordPages(XWPFDocument document) {
        int paragraphs = document.getParagraphs().size();
        int tables = document.getTables().size();
        int images = 0;

        for (XWPFParagraph paragraph : document.getParagraphs()) {
            for (XWPFRun run : paragraph.getRuns()) {
                images += run.getEmbeddedPictures().size();
            }
        }

        int contentUnits = paragraphs + (tables * 5) + (images * 3);
        int estimatedPages = Math.max(1, (int) Math.ceil(contentUnits / 25.0));

        log.debug("Word estimation - Paragraphs: {}, Tables: {}, Images: {}, Estimated pages: {}",
                paragraphs, tables, images, estimatedPages);

        return estimatedPages;
    }

    private Integer estimateDocPages(Range range) {
        final int CHARS_PER_PAGE = 3000;
        int textLength = range.text().length();
        int estimatedPages = Math.max(1, (int) Math.ceil(textLength / (double) CHARS_PER_PAGE));

        log.debug("DOC estimation - Characters: {}, Estimated pages: {}", textLength, estimatedPages);
        return estimatedPages;
    }
}

