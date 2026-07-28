package com.pji.triagem.service.impl;

import com.pji.triagem.base.repository.BaseRepository;
import com.pji.triagem.base.service.impl.BaseServiceImpl;
import com.pji.triagem.model.AssessmentSymptom;
import com.pji.triagem.repository.AssessmentSymptomRepository;
import com.pji.triagem.service.AssessmentSymptomService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class AssessmentSymptomServiceImpl extends BaseServiceImpl<AssessmentSymptom> implements AssessmentSymptomService {

    private final AssessmentSymptomRepository assessmentSymptomRepository;

    @Override
    protected BaseRepository<AssessmentSymptom, Long> getRepository() {
        return assessmentSymptomRepository;
    }

    @Override
    protected String getResourceName() {
        return "Avaliação de sintoma";
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssessmentSymptom> findByAssessment(Long assessmentId) {
        return assessmentSymptomRepository.findByAssessmentIdOrderByIdAsc(assessmentId);
    }
}
