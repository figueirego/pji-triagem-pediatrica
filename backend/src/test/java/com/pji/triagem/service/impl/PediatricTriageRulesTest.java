package com.pji.triagem.service.impl;

import com.pji.triagem.exception.ValidationException;
import com.pji.triagem.model.Classification;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class PediatricTriageRulesTest {
    private final LocalDate today = LocalDate.of(2026, 9, 24);
    private Map<String, String> well() {
        return Map.of("FEBRE_TEMPERATURA", "ENTRE_38_5_39", "FEBRE_BOM_ESTADO", "YES",
                "UNIVERSAL_PROSTRACAO", "NO", "UNIVERSAL_RESPIRACAO", "NO");
    }
    private PediatricTriageRules.Decision fever(LocalDate dob, Map<String, String> answers) {
        return PediatricTriageRules.evaluate("FEBRE", dob, today, answers, 6, false);
    }
    @Test void birthdayCannotBeBypassedByNoAnswer() {
        assertEquals(Classification.HIGH, fever(today.minusMonths(3).plusDays(1), well()).classification());
        assertEquals(Classification.LOW, fever(today.minusMonths(3), well()).classification());
        assertEquals(Classification.LOW, fever(today.minusMonths(3).minusDays(1), well()).classification());
    }
    @Test void highTemperatureAloneRequiresMedicalEvaluation() {
        var answers = new HashMap<>(well());
        answers.put("FEBRE_TEMPERATURA", "ACIMA_39");
        assertEquals(Classification.MOD, fever(today.minusYears(2), answers).classification());
        answers.put("UNIVERSAL_PROSTRACAO", "YES");
        assertEquals(Classification.HIGH, fever(today.minusYears(2), answers).classification());
    }
    @Test void exact39SafeguardForYoungInfants() {
        var answers = new HashMap<>(well());
        answers.put("FEBRE_TEMPERATURA", "EXATOS_39");
        assertEquals(Classification.MOD, fever(today.minusMonths(3), answers).classification());
        assertEquals(Classification.MOD, fever(today.minusMonths(6), answers).classification());
        assertEquals(Classification.LOW, fever(today.minusMonths(6).minusDays(1), answers).classification());
    }
    @ParameterizedTest @ValueSource(strings = {"UNIVERSAL_PROSTRACAO", "UNIVERSAL_RESPIRACAO", "UNIVERSAL_HIDRATACAO", "UNIVERSAL_MANCHAS"})
    void everyUniversalEmergencyOverridesGoodState(String code) {
        var answers = new HashMap<>(well());
        answers.put(code, "YES");
        assertEquals(Classification.HIGH, fever(today.minusYears(2), answers).classification());
    }
    @ParameterizedTest @ValueSource(strings = {"FEBRE", "TOSSE", "VOMITOS", "DIARREIA", "DOR_ABDOMINAL", "FALTA_DE_AR", "MANCHAS_PELE", "TRAUMA_LEVE", "DOR_OUVIDO"})
    void uncertaintyCannotReturnLowAndExistingRedFlagsRemainHigh(String symptom) {
        assertEquals(Classification.MOD, PediatricTriageRules.evaluate(symptom, today.minusYears(2), today,
                Map.of("UNIVERSAL_RESPIRACAO", "DUNNO"), 0, false).classification());
        assertEquals(Classification.HIGH, PediatricTriageRules.evaluate(symptom, today.minusYears(2), today,
                Map.of(), 0, true).classification());
    }
    @Test void homeObservationRequiresAffirmativeGoodState() {
        assertEquals(Classification.MOD, fever(today.minusYears(2), Map.of()).classification());
        assertEquals(Classification.MOD, fever(today.minusYears(2), Map.of("FEBRE_BOM_ESTADO", "NO")).classification());
        var answers = new HashMap<>(well());
        answers.put("FEBRE_MAIS_3_DIAS", "YES");
        assertEquals(Classification.MOD, fever(today.minusYears(2), answers).classification());
    }
    @Test void calendarMonthBoundariesIncludeLeapYearsAndMonthEnds() {
        assertEquals(Classification.HIGH, PediatricTriageRules.evaluate("FEBRE", LocalDate.of(2024, 2, 29),
                LocalDate.of(2024, 5, 28), well(), 0, false).classification());
        assertEquals(Classification.LOW, PediatricTriageRules.evaluate("FEBRE", LocalDate.of(2024, 2, 29),
                LocalDate.of(2024, 5, 29), well(), 0, false).classification());
        assertEquals(Classification.LOW, PediatricTriageRules.evaluate("FEBRE", LocalDate.of(2026, 1, 31),
                LocalDate.of(2026, 4, 30), well(), 0, false).classification());
    }
    @Test void unmeasuredTemperatureIsNeverLow() {
        var answers = new HashMap<>(well());
        answers.put("FEBRE_TEMPERATURA", "NAO_MEDIDA");
        assertEquals(Classification.MOD, fever(today.minusYears(2), answers).classification());
    }
    @Test void ageLimitsAreEvaluatedOnAssessmentDate() {
        assertDoesNotThrow(() -> PediatricTriageRules.validateAge(today.minusYears(13).plusDays(1), today));
        assertDoesNotThrow(() -> PediatricTriageRules.validateAge(today, today));
        assertThrows(ValidationException.class, () -> PediatricTriageRules.validateAge(today.minusYears(13), today));
        assertThrows(ValidationException.class, () -> PediatricTriageRules.validateAge(today.plusDays(1), today));
        assertThrows(ValidationException.class, () -> PediatricTriageRules.validateAge(null, today));
    }
    @Test void genericScoreLevelsRemainCompatible() {
        for (int score : new int[]{0,3,6}) {
            assertEquals(score < 3 ? Classification.LOW : score < 6 ? Classification.MOD : Classification.HIGH,
                PediatricTriageRules.evaluate("TOSSE", today.minusYears(1), today, Map.of(), score, false).classification());
        }
    }
}
