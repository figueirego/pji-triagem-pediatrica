package com.pji.triagem.service;

import com.pji.triagem.base.service.BaseService;
import com.pji.triagem.dto.request.QuestionnaireRequest;
import com.pji.triagem.dto.response.QuestionnaireResponse;
import com.pji.triagem.dto.response.SymptomResponse;
import com.pji.triagem.model.Symptom;

import java.util.List;

public interface SymptomService extends BaseService<Symptom> {

    List<SymptomResponse> findAllSymptoms();

    QuestionnaireResponse buildQuestionnaire(QuestionnaireRequest request);
}
