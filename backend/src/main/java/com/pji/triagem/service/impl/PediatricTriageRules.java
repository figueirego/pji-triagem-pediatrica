package com.pji.triagem.service.impl;

import com.pji.triagem.exception.ValidationException;
import com.pji.triagem.model.Classification;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

/** Educational project rules, not a clinically validated diagnostic protocol. */
public final class PediatricTriageRules {
    public static final String VERSION = "projeto-2.2-educacional";
    private static final Set<String> EMERGENCY_QUESTIONS = Set.of(
            "UNIVERSAL_PROSTRACAO", "UNIVERSAL_RESPIRACAO", "UNIVERSAL_HIDRATACAO", "UNIVERSAL_MANCHAS");
    private PediatricTriageRules() { }

    public record Decision(Classification classification, String reason) { }

    public static void validateAge(LocalDate birthDate, LocalDate date) {
        if (birthDate == null || birthDate.isAfter(date) || !birthDate.plusYears(13).isAfter(date)) {
            throw new ValidationException("A avaliação é destinada a crianças de 0 a 12 anos; confira a data de nascimento.");
        }
    }

    public static Decision evaluate(String symptom, LocalDate birthDate, LocalDate date,
                                    Map<String, String> answers, int score, boolean redFlag) {
        validateAge(birthDate, date);
        if ("FEBRE".equals(symptom) && birthDate.plusMonths(3).isAfter(date)) {
            return new Decision(Classification.HIGH, "Febre informada em bebê com menos de 3 meses.");
        }
        if (redFlag || EMERGENCY_QUESTIONS.stream().anyMatch(code -> "YES".equals(answers.get(code)))) {
            return new Decision(Classification.HIGH, "Foi informado um sinal de alerta que exige atendimento imediato.");
        }
        boolean unknown = answers.containsValue("DUNNO") || answers.containsValue("NAO_MEDIDA");
        if ("FEBRE".equals(symptom)) {
            return fever(birthDate, date, answers, unknown);
        }
        Classification level = score >= 6 ? Classification.HIGH : score >= 3 || unknown ? Classification.MOD : Classification.LOW;
        return new Decision(level, switch (level) {
            case HIGH -> "Os sinais informados neste questionário exigem atendimento imediato.";
            case MOD -> unknown ? "Há informações incertas; procure avaliação médica nas próximas horas." : "Os sintomas informados precisam de avaliação médica em até 24 horas.";
            case LOW -> "As respostas não indicaram sinais de alerta neste momento; observe a evolução.";
        });
    }

    private static Decision fever(LocalDate birthDate, LocalDate date, Map<String, String> answers, boolean unknown) {
        String temperature = answers.get("FEBRE_TEMPERATURA");
        boolean youngInfant39 = "EXATOS_39".equals(temperature) && !birthDate.plusMonths(6).isBefore(date);
        if ("ACIMA_39".equals(temperature) || youngInfant39) {
            return new Decision(Classification.MOD, "A temperatura informada exige avaliação médica nas próximas horas, em até 24 horas.");
        }
        if (unknown || !"YES".equals(answers.get("FEBRE_BOM_ESTADO")) || "YES".equals(answers.get("FEBRE_MAIS_3_DIAS"))) {
            return new Decision(Classification.MOD, "Febre persistente, estado geral alterado ou informações incertas precisam de avaliação médica em até 24 horas.");
        }
        return new Decision(Classification.LOW, "Bom estado geral informado, sem sinais de alerta; observe em casa e procure ajuda se houver piora.");
    }
}
