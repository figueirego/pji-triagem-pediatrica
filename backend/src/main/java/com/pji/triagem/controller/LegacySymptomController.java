package com.pji.triagem.controller;

import com.pji.triagem.base.dto.ResponseDTO;
import com.pji.triagem.base.service.ResponseService;
import com.pji.triagem.dto.request.QuestionnaireRequest;
import com.pji.triagem.dto.response.QuestionResponse;
import com.pji.triagem.dto.response.QuestionnaireResponse;
import com.pji.triagem.dto.response.SymptomResponse;
import com.pji.triagem.service.SymptomService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sintomas")
@AllArgsConstructor
public class LegacySymptomController {

    private final ResponseService responseService;
    private final SymptomService symptomService;

    @GetMapping
    public ResponseEntity<ResponseDTO<List<SymptomResponse>>> list() {
        return responseService.ok(symptomService.findAllSymptoms());
    }

    @GetMapping("/{symptomId}/perguntas")
    public ResponseEntity<ResponseDTO<List<QuestionResponse>>> questions(@PathVariable Long symptomId) {
        QuestionnaireRequest request = new QuestionnaireRequest();
        request.setSymptomIds(List.of(symptomId));
        QuestionnaireResponse questionnaire = symptomService.buildQuestionnaire(request);
        List<QuestionResponse> questions = questionnaire.getSymptoms().isEmpty()
                ? List.of()
                : questionnaire.getSymptoms().get(0).getQuestions();
        return responseService.ok(questions);
    }
}
