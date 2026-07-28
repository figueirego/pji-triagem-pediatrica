package com.pji.triagem.service.impl;

import com.pji.triagem.exception.ValidationException;
import com.pji.triagem.model.Classification;
import com.pji.triagem.model.YesNoAnswer;
import com.pji.triagem.model.YesNoWeight;
import com.pji.triagem.service.AssessmentCalculationService;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class AssessmentCalculationServiceImpl implements AssessmentCalculationService {

    @Override
    public Integer scoreYesNo(YesNoAnswer answer, YesNoWeight weight) {
        if (answer == null) {
            throw new ValidationException("Resposta YESNO é obrigatória");
        }
        if (weight == null) {
            throw new ValidationException("Peso YESNO não encontrado");
        }

        return switch (answer) {
            case YES -> weight.getYesScore();
            case NO -> weight.getNoScore();
            case DUNNO -> weight.getUnknownScore();
        };
    }

    @Override
    public Boolean redFlagYesNo(YesNoAnswer answer, YesNoWeight weight) {
        if (answer == null || weight == null) {
            return false;
        }

        return switch (answer) {
            case YES -> Boolean.TRUE.equals(weight.getRedFlagOnYes());
            case NO -> Boolean.TRUE.equals(weight.getRedFlagOnNo());
            case DUNNO -> false;
        };
    }

    @Override
    public Classification classifySymptom(Integer score, Boolean redFlagDetected) {
        if (Boolean.TRUE.equals(redFlagDetected)) {
            return Classification.HIGH;
        }

        int safeScore = score == null ? 0 : score;
        if (safeScore < 3) {
            return Classification.LOW;
        }
        if (safeScore <= 5) {
            return Classification.MOD;
        }
        return Classification.HIGH;
    }

    @Override
    public Classification classifyFinal(Collection<Classification> classifications) {
        if (classifications == null || classifications.isEmpty()) {
            throw new ValidationException("Avaliação sem sintomas");
        }
        if (classifications.contains(Classification.HIGH)) {
            return Classification.HIGH;
        }
        if (classifications.contains(Classification.MOD)) {
            return Classification.MOD;
        }
        return Classification.LOW;
    }
}
