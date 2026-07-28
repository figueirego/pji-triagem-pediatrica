package com.pji.triagem.service;

import com.pji.triagem.base.service.BaseService;
import com.pji.triagem.model.Question;

import java.util.List;

public interface QuestionService extends BaseService<Question> {

    List<Question> findBySymptom(Long symptomId);

    List<Question> findBySymptoms(List<Long> symptomIds);
}
