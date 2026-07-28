package com.pji.triagem.service.impl;

import com.pji.triagem.model.Classification;
import com.pji.triagem.model.YesNoAnswer;
import com.pji.triagem.model.YesNoWeight;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AssessmentCalculationServiceImplTest {

    private final AssessmentCalculationServiceImpl service = new AssessmentCalculationServiceImpl();

    @Test
    void classifySymptomUsesRedFlagAsHighPriority() {
        assertEquals(Classification.HIGH, service.classifySymptom(0, true));
    }

    @Test
    void classifySymptomByScoreRanges() {
        assertEquals(Classification.LOW, service.classifySymptom(2, false));
        assertEquals(Classification.MOD, service.classifySymptom(3, false));
        assertEquals(Classification.MOD, service.classifySymptom(5, false));
        assertEquals(Classification.HIGH, service.classifySymptom(6, false));
    }

    @ParameterizedTest(name = "{0} score {1} -> {2}")
    @CsvSource({
            "FEBRE,2,LOW", "FEBRE,3,MOD", "FEBRE,6,HIGH",
            "TOSSE,2,LOW", "TOSSE,3,MOD", "TOSSE,6,HIGH",
            "VOMITOS,2,LOW", "VOMITOS,3,MOD", "VOMITOS,6,HIGH",
            "DIARREIA,2,LOW", "DIARREIA,3,MOD", "DIARREIA,6,HIGH",
            "DOR_ABDOMINAL,2,LOW", "DOR_ABDOMINAL,3,MOD", "DOR_ABDOMINAL,6,HIGH",
            "FALTA_DE_AR,2,LOW", "FALTA_DE_AR,3,MOD", "FALTA_DE_AR,6,HIGH",
            "MANCHAS_PELE,2,LOW", "MANCHAS_PELE,3,MOD", "MANCHAS_PELE,6,HIGH",
            "TRAUMA_LEVE,2,LOW", "TRAUMA_LEVE,3,MOD", "TRAUMA_LEVE,6,HIGH",
            "DOR_OUVIDO,2,LOW", "DOR_OUVIDO,3,MOD", "DOR_OUVIDO,6,HIGH"
    })
    void classifiesEachSeededSymptomRiskLevel(String symptomCode, int score, Classification expected) {
        assertFalse(symptomCode.isBlank());
        assertEquals(expected, service.classifySymptom(score, false));
    }

    @ParameterizedTest(name = "{0} red flag -> HIGH")
    @ValueSource(strings = {
            "FEBRE", "TOSSE", "VOMITOS", "DIARREIA", "DOR_ABDOMINAL",
            "FALTA_DE_AR", "MANCHAS_PELE", "TRAUMA_LEVE", "DOR_OUVIDO"
    })
    void classifiesEachSeededSymptomRedFlagAsHigh(String symptomCode) {
        assertFalse(symptomCode.isBlank());
        assertEquals(Classification.HIGH, service.classifySymptom(0, true));
    }

    @Test
    void classifyFinalUsesHighestSeverity() {
        assertEquals(Classification.HIGH, service.classifyFinal(List.of(Classification.LOW, Classification.HIGH)));
        assertEquals(Classification.MOD, service.classifyFinal(List.of(Classification.LOW, Classification.MOD)));
        assertEquals(Classification.LOW, service.classifyFinal(List.of(Classification.LOW)));
    }

    @Test
    void scoreAndRedFlagYesNoUseConfiguredWeights() {
        YesNoWeight weight = new YesNoWeight();
        weight.setYesScore(8);
        weight.setNoScore(0);
        weight.setUnknownScore(2);
        weight.setRedFlagOnYes(true);
        weight.setRedFlagOnNo(false);

        assertEquals(8, service.scoreYesNo(YesNoAnswer.YES, weight));
        assertEquals(0, service.scoreYesNo(YesNoAnswer.NO, weight));
        assertEquals(2, service.scoreYesNo(YesNoAnswer.DUNNO, weight));
        assertTrue(service.redFlagYesNo(YesNoAnswer.YES, weight));
        assertFalse(service.redFlagYesNo(YesNoAnswer.NO, weight));
        assertFalse(service.redFlagYesNo(YesNoAnswer.DUNNO, weight));
    }
}
