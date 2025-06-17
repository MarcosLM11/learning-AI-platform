package com.marcos.studyasistant.documentservice.reposiroty;

import com.marcos.studyasistant.documentservice.entity.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface DocumentsRepository extends JpaRepository<DocumentEntity, UUID> {
}
