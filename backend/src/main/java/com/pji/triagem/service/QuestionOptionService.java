package com.pji.triagem.service;

import com.pji.triagem.base.service.BaseService;
import com.pji.triagem.model.QuestionOption;

import java.util.List;

public interface QuestionOptionService extends BaseService<QuestionOption> {

    List<QuestionOption> findByQuestion(Long questionId);

    List<QuestionOption> findByQuestions(List<Long> questionIds);

    QuestionOption findByQuestionOption(Long questionId, Long optionId);
}
