package com.pji.triagem.repository;

import com.pji.triagem.base.repository.BaseRepository;
import com.pji.triagem.model.Question;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionRepository extends BaseRepository<Question, Long> {

    List<Question> findBySymptomIdOrderByOrderAsc(Long symptomId);

    @Query("""
            SELECT q
            FROM Question q
            WHERE q.symptom.id IN :symptomIds
            ORDER BY q.symptom.order ASC, q.symptom.id ASC, q.order ASC, q.id ASC
            """)
    List<Question> findBySymptomIdInOrderBySymptomOrderAscOrderAsc(@Param("symptomIds") List<Long> symptomIds);
}
