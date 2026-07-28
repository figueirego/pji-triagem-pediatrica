package com.pji.triagem.repository;

import com.pji.triagem.base.repository.BaseRepository;
import com.pji.triagem.model.AssessmentSymptom;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AssessmentSymptomRepository extends BaseRepository<AssessmentSymptom, Long> {

    @Query("""
            SELECT assessmentSymptom
            FROM AssessmentSymptom assessmentSymptom
            JOIN FETCH assessmentSymptom.assessment
            JOIN FETCH assessmentSymptom.symptom
            WHERE assessmentSymptom.assessment.id = :assessmentId
            ORDER BY assessmentSymptom.id ASC
            """)
    List<AssessmentSymptom> findByAssessmentIdOrderByIdAsc(@Param("assessmentId") Long assessmentId);

    @Query("""
            SELECT assessmentSymptom
            FROM AssessmentSymptom assessmentSymptom
            JOIN FETCH assessmentSymptom.assessment
            JOIN FETCH assessmentSymptom.symptom
            WHERE assessmentSymptom.assessment.id IN :assessmentIds
            ORDER BY assessmentSymptom.assessment.id ASC, assessmentSymptom.id ASC
            """)
    List<AssessmentSymptom> findByAssessmentIdInOrderByAssessmentIdAscIdAsc(
            @Param("assessmentIds") List<Long> assessmentIds
    );
}
