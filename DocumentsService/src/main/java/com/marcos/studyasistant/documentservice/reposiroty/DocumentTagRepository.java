package com.marcos.studyasistant.documentservice.reposiroty;

import com.marcos.studyasistant.documentservice.entity.DocumentTag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentTagRepository extends JpaRepository<DocumentTag, UUID> {

    List<DocumentTag> findByDocumentIdOrderByConfidenceScoreDesc(UUID documentId);

    @Query("SELECT dt.tag, COUNT(dt) FROM DocumentTag dt GROUP BY dt.tag ORDER BY COUNT(dt) DESC")
    List<Object[]> findMostUsedTags(Pageable pageable);

    @Query("SELECT dt FROM DocumentTag dt WHERE dt.document.userId = :userId AND dt.tag = :tag")
    List<DocumentTag> findByUserIdAndTag(@Param("userId") UUID userId, @Param("tag") String tag);
}
