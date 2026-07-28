package com.pji.triagem.service.impl;

import com.pji.triagem.base.repository.BaseRepository;
import com.pji.triagem.base.service.impl.BaseServiceImpl;
import com.pji.triagem.model.Question;
import com.pji.triagem.repository.QuestionRepository;
import com.pji.triagem.service.QuestionService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class QuestionServiceImpl extends BaseServiceImpl<Question> implements QuestionService {

    private final QuestionRepository questionRepository;

    @Override
    protected BaseRepository<Question, Long> getRepository() {
        return questionRepository;
    }

    @Override
    protected String getResourceName() {
        return "Pergunta";
    }

    @Override
    @Transactional(readOnly = true)
    public List<Question> findBySymptom(Long symptomId) {
        return questionRepository.findBySymptomIdOrderByOrderAsc(symptomId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Question> findBySymptoms(List<Long> symptomIds) {
        return questionRepository.findBySymptomIdInOrderBySymptomOrderAscOrderAsc(symptomIds);
    }
}
