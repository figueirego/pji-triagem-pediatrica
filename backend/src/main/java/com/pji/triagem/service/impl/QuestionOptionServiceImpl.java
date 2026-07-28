package com.pji.triagem.service.impl;

import com.pji.triagem.base.repository.BaseRepository;
import com.pji.triagem.base.service.impl.BaseServiceImpl;
import com.pji.triagem.exception.ValidationException;
import com.pji.triagem.model.QuestionOption;
import com.pji.triagem.repository.QuestionOptionRepository;
import com.pji.triagem.service.QuestionOptionService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class QuestionOptionServiceImpl extends BaseServiceImpl<QuestionOption> implements QuestionOptionService {

    private final QuestionOptionRepository questionOptionRepository;

    @Override
    protected BaseRepository<QuestionOption, Long> getRepository() {
        return questionOptionRepository;
    }

    @Override
    protected String getResourceName() {
        return "Opção de pergunta";
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestionOption> findByQuestion(Long questionId) {
        return questionOptionRepository.findByQuestionIdOrderByOrderAsc(questionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestionOption> findByQuestions(List<Long> questionIds) {
        return questionOptionRepository.findByQuestionIdInOrderByOrderAsc(questionIds);
    }

    @Override
    @Transactional(readOnly = true)
    public QuestionOption findByQuestionOption(Long questionId, Long optionId) {
        return questionOptionRepository.findByIdAndQuestionId(optionId, questionId)
                .orElseThrow(() -> new ValidationException("Opção não pertence à pergunta informada"));
    }
}
