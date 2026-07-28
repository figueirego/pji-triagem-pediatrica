package com.pji.triagem.service;

import com.pji.triagem.base.service.BaseService;
import com.pji.triagem.model.AssessmentSymptom;

import java.util.List;

public interface AssessmentSymptomService extends BaseService<AssessmentSymptom> {

    List<AssessmentSymptom> findByAssessment(Long assessmentId);
}
