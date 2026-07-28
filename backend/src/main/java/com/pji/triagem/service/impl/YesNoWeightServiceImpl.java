package com.pji.triagem.service.impl;

import com.pji.triagem.base.repository.BaseRepository;
import com.pji.triagem.base.service.impl.BaseServiceImpl;
import com.pji.triagem.exception.ValidationException;
import com.pji.triagem.model.YesNoWeight;
import com.pji.triagem.repository.YesNoWeightRepository;
import com.pji.triagem.service.YesNoWeightService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class YesNoWeightServiceImpl extends BaseServiceImpl<YesNoWeight> implements YesNoWeightService {

    private final YesNoWeightRepository yesNoWeightRepository;

    @Override
    protected BaseRepository<YesNoWeight, Long> getRepository() {
        return yesNoWeightRepository;
    }

    @Override
    protected String getResourceName() {
        return "Peso YESNO";
    }

    @Override
    @Transactional(readOnly = true)
    public YesNoWeight findByQuestion(Long questionId) {
        return yesNoWeightRepository.findByQuestionId(questionId)
                .orElseThrow(() -> new ValidationException("Peso YESNO não encontrado para a pergunta"));
    }
}
