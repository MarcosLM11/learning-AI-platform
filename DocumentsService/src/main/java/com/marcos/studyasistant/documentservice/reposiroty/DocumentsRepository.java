package com.marcos.studyasistant.documentservice.reposiroty;

import com.marcos.studyasistant.documentservice.entity.DocumentEntity;
import com.marcos.studyasistant.documentservice.entity.enums.ProcessingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentsRepository extends JpaRepository<DocumentEntity, UUID> {

    @Query("SELECT d FROM DocumentEntity d WHERE d.userId = :userId ORDER BY d.createdAt DESC")
    Page<DocumentEntity> findByUserIdOrderByCreatedAtDesc(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT d FROM DocumentEntity d WHERE d.status = :status")
    List<DocumentEntity> findByStatus(@Param("status") ProcessingStatus status);

    @Query("SELECT COUNT(d) FROM DocumentEntity d WHERE d.userId = :userId AND d.status = :status")
    Long countByUserIdAndStatus(@Param("userId") UUID userId, @Param("status") ProcessingStatus status);

    @Query("SELECT d FROM DocumentEntity d WHERE d.createdAt BETWEEN :startDate AND :endDate")
    List<DocumentEntity> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);
}
