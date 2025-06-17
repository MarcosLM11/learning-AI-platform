package com.marcos.studyasistant.documentservice.entity;

import com.marcos.studyasistant.documentservice.entity.enums.ProcessingStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "documents")
@Getter @Setter @Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @Column(nullable = false)
    private String filename;

    @Column(nullable = false)
    private String originalFilename;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false)
    private String storagePath;

    @Column(columnDefinition = "TEXT")
    private String extractedText;

    @Enumerated(EnumType.STRING)
    private ProcessingStatus status;

    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    private LocalDateTime processedAt;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;
}
