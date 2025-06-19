package com.marcos.studyasistant.documentservice.entity;

import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "document_processing_logs")
@Getter @Setter @Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentProcessingLog {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private DocumentEntity document;

    @Column(name = "processing_step", nullable = false, length = 50)
    private String processingStep;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Type(JsonType.class)
    @Column(name = "details", columnDefinition = "jsonb")
    private Map<String, Object> details;


    @Column(name = "processing_time_ms")
    private Integer processingTimeMs;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
