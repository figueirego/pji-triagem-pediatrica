package com.pji.triagem.service.impl;

import com.pji.triagem.model.Question;
import com.pji.triagem.model.QuestionType;
import com.pji.triagem.model.YesNoAnswer;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class AgeConditionResolverTest {
    private final LocalDate birth = LocalDate.of(2024, 2, 29);
    private Question upper(int value, String unit, boolean inclusive) {
        Question question = new Question();
        question.setCode("QUALQUER_CODIGO_SEM_NUMERO");
        question.setType(QuestionType.YESNO);
        question.setAgeDerived(true);
        question.setAgeUpperValue(value);
        question.setAgeUpperUnit(unit);
        question.setAgeUpperInclusive(inclusive);
        return question;
    }
    @Test void arbitrarySixMonthConditionUsesCalendarBoundary() {
        Question question = upper(6, "MONTHS", false);
        assertTrue(AgeConditionResolver.isDerived(question));
        assertEquals(YesNoAnswer.YES, AgeConditionResolver.resolve(question, birth, LocalDate.of(2024, 8, 28)));
        assertEquals(YesNoAnswer.NO, AgeConditionResolver.resolve(question, birth, LocalDate.of(2024, 8, 29)));
    }
    @Test void twoYearInclusiveConditionHandlesLeapDay() {
        Question question = upper(2, "YEARS", true);
        assertEquals(YesNoAnswer.YES, AgeConditionResolver.resolve(question, birth, LocalDate.of(2026, 2, 28)));
        assertEquals(YesNoAnswer.NO, AgeConditionResolver.resolve(question, birth, LocalDate.of(2026, 3, 1)));
    }
    @Test void mixedUnitRangeHonorsBothEdges() {
        Question question = upper(2, "YEARS", false);
        question.setAgeLowerValue(6);
        question.setAgeLowerUnit("MONTHS");
        question.setAgeLowerInclusive(true);
        assertEquals(YesNoAnswer.NO, AgeConditionResolver.resolve(question, birth, birth.plusMonths(6).minusDays(1)));
        assertEquals(YesNoAnswer.YES, AgeConditionResolver.resolve(question, birth, birth.plusMonths(6)));
        assertEquals(YesNoAnswer.YES, AgeConditionResolver.resolve(question, birth, birth.plusYears(2).minusDays(1)));
        assertEquals(YesNoAnswer.NO, AgeConditionResolver.resolve(question, birth, birth.plusYears(2)));
    }
    @Test void lowerOnlyDaysConditionHonorsExclusiveBoundary() {
        Question question = new Question();
        question.setType(QuestionType.YESNO);
        question.setAgeDerived(true);
        question.setAgeLowerValue(30);
        question.setAgeLowerUnit("DAYS");
        question.setAgeLowerInclusive(false);
        assertEquals(YesNoAnswer.NO, AgeConditionResolver.resolve(question, birth, birth.plusDays(30)));
        assertEquals(YesNoAnswer.YES, AgeConditionResolver.resolve(question, birth, birth.plusDays(31)));
    }
    @Test void ordinaryQuestionCannotBeInferredFromWording() {
        Question question = new Question();
        question.setCode("IDADE_MENOR_6_MESES");
        question.setText("A criança tem menos de seis meses?");
        question.setType(QuestionType.YESNO);
        assertFalse(AgeConditionResolver.isDerived(question));
        assertThrows(IllegalStateException.class, () -> AgeConditionResolver.resolve(question, birth, birth));
    }
    @Test void malformedMetadataFailsClosed() {
        Question question = upper(6, "WEEKS", false);
        assertThrows(IllegalStateException.class, () -> AgeConditionResolver.isDerived(question));
        question.setAgeUpperUnit("MONTHS");
        question.setAgeUpperValue(-1);
        assertThrows(IllegalStateException.class, () -> AgeConditionResolver.isDerived(question));
        question.setAgeUpperValue(6);
        question.setAgeUpperInclusive(null);
        assertThrows(IllegalStateException.class, () -> AgeConditionResolver.isDerived(question));
        question.setAgeUpperInclusive(false);
        question.setType(QuestionType.OPTIONS);
        assertThrows(IllegalStateException.class, () -> AgeConditionResolver.isDerived(question));
        question.setType(QuestionType.YESNO);
        question.setAgeDerived(false);
        assertThrows(IllegalStateException.class, () -> AgeConditionResolver.isDerived(question));
    }
    @Test void missingOrInvertedAndEmptyBoundsFailClosed() {
        Question missing = new Question();
        missing.setAgeDerived(true);
        missing.setType(QuestionType.YESNO);
        assertThrows(IllegalStateException.class, () -> AgeConditionResolver.isDerived(missing));
        Question inverted = upper(6, "MONTHS", false);
        inverted.setAgeLowerValue(2);
        inverted.setAgeLowerUnit("YEARS");
        inverted.setAgeLowerInclusive(true);
        assertThrows(IllegalStateException.class, () -> AgeConditionResolver.resolve(inverted, birth, birth));
        inverted.setAgeLowerValue(6);
        inverted.setAgeLowerUnit("MONTHS");
        assertThrows(IllegalStateException.class, () -> AgeConditionResolver.resolve(inverted, birth, birth));
    }
    @Test void invalidDatesFailClosed() {
        Question question = upper(3, "MONTHS", false);
        assertThrows(IllegalStateException.class, () -> AgeConditionResolver.resolve(question, null, birth));
        assertThrows(IllegalStateException.class, () -> AgeConditionResolver.resolve(question, birth, birth.minusDays(1)));
    }
}
