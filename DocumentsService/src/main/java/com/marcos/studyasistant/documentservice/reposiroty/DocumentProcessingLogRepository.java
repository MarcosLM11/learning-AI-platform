package com.marcos.studyasistant.documentservice.reposiroty;

import com.marcos.studyasistant.documentservice.entity.DocumentProcessingLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentProcessingLogRepository extends JpaRepository<DocumentProcessingLog, UUID> {

    List<DocumentProcessingLog> findByDocumentIdOrderByCreatedAtDesc(UUID documentId);

    @Query("SELECT dpl FROM DocumentProcessingLog dpl WHERE dpl.document.id = :documentId AND dpl.processingStep = :step")
    Optional<DocumentProcessingLog> findByDocumentIdAndProcessingStep(@Param("documentId") UUID documentId,
                                                                      @Param("step") String step);

    @Query("SELECT dpl FROM DocumentProcessingLog dpl WHERE dpl.status = 'ERROR' AND dpl.createdAt >= :since")
    List<DocumentProcessingLog> findErrorsSince(@Param("since") LocalDateTime since);
}
