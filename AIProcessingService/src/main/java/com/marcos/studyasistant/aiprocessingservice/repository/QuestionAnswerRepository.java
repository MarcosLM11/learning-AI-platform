package com.marcos.studyasistant.aiprocessingservice.repository;

import com.marcos.studyasistant.aiprocessingservice.entity.QuestionAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuestionAnswerRepository extends JpaRepository<QuestionAnswer, UUID> {
    
    List<QuestionAnswer> findByDocumentQAId(UUID documentQAId);
    
    List<QuestionAnswer> findByDifficultyLevel(String difficultyLevel);
    
    List<QuestionAnswer> findByQuestionType(String questionType);
}