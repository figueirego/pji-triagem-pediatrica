package com.pji.triagem.service.impl;

import com.pji.triagem.model.Question;
import com.pji.triagem.model.QuestionType;
import com.pji.triagem.model.YesNoAnswer;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.Set;

/** Explicit question metadata only; wording and question codes never determine age conditions. */
public final class AgeConditionResolver {
    private static final Set<String> UNITS = Set.of("DAYS", "MONTHS", "YEARS");
    private AgeConditionResolver() { }

    public static boolean isDerived(Question question) {
        boolean lower = validateBound(question.getAgeLowerValue(), question.getAgeLowerUnit(), question.getAgeLowerInclusive());
        boolean upper = validateBound(question.getAgeUpperValue(), question.getAgeUpperUnit(), question.getAgeUpperInclusive());
        if (!Boolean.TRUE.equals(question.getAgeDerived())) {
            if (lower || upper) {
                throw invalid();
            }
            return false;
        }
        if (question.getType() != QuestionType.YESNO || (!lower && !upper)) {
            throw invalid();
        }
        return true;
    }

    public static YesNoAnswer resolve(Question question, LocalDate birthDate, LocalDate assessedOn) {
        if (!isDerived(question) || birthDate == null || assessedOn == null || assessedOn.isBefore(birthDate)) {
            throw invalid();
        }
        LocalDate lower = anniversary(birthDate, question.getAgeLowerValue(), question.getAgeLowerUnit());
        LocalDate upper = anniversary(birthDate, question.getAgeUpperValue(), question.getAgeUpperUnit());
        if (lower != null && upper != null && (lower.isAfter(upper)
                || (lower.equals(upper) && (!question.getAgeLowerInclusive() || !question.getAgeUpperInclusive())))) {
            throw invalid();
        }
        boolean meetsLower = lower == null || assessedOn.isAfter(lower)
                || (assessedOn.equals(lower) && question.getAgeLowerInclusive());
        boolean meetsUpper = upper == null || assessedOn.isBefore(upper)
                || (assessedOn.equals(upper) && question.getAgeUpperInclusive());
        return meetsLower && meetsUpper ? YesNoAnswer.YES : YesNoAnswer.NO;
    }

    private static boolean validateBound(Integer value, String unit, Boolean inclusive) {
        if (value == null && unit == null && inclusive == null) {
            return false;
        }
        if (value == null || value < 0 || unit == null || !UNITS.contains(unit) || inclusive == null) {
            throw invalid();
        }
        return true;
    }

    private static LocalDate anniversary(LocalDate birthDate, Integer value, String unit) {
        if (value == null) {
            return null;
        }
        try {
            return switch (unit) {
                case "DAYS" -> birthDate.plusDays(value);
                case "MONTHS" -> birthDate.plusMonths(value);
                case "YEARS" -> birthDate.plusYears(value);
                default -> throw invalid();
            };
        } catch (DateTimeException ex) {
            throw invalid();
        }
    }

    private static IllegalStateException invalid() {
        return new IllegalStateException("Configuração inválida de condição por idade; avaliação interrompida.");
    }
}
