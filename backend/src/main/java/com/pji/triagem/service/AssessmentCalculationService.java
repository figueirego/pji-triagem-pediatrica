package com.pji.triagem.service;

import com.pji.triagem.model.Classification;
import com.pji.triagem.model.YesNoAnswer;
import com.pji.triagem.model.YesNoWeight;

import java.util.Collection;

public interface AssessmentCalculationService {

    Integer scoreYesNo(YesNoAnswer answer, YesNoWeight weight);

    Boolean redFlagYesNo(YesNoAnswer answer, YesNoWeight weight);

    Classification classifySymptom(Integer score, Boolean redFlagDetected);

    Classification classifyFinal(Collection<Classification> classifications);
}
