package com.pji.triagem.service.impl;

import com.pji.triagem.base.repository.BaseRepository;
import com.pji.triagem.base.service.impl.BaseServiceImpl;
import com.pji.triagem.dto.request.QuestionnaireRequest;
import com.pji.triagem.dto.response.QuestionResponse;
import com.pji.triagem.dto.response.QuestionnaireResponse;
import com.pji.triagem.dto.response.QuestionnaireSymptomResponse;
import com.pji.triagem.dto.response.SymptomResponse;
import com.pji.triagem.exception.ValidationException;
import com.pji.triagem.mapper.QuestionMapper;
import com.pji.triagem.mapper.QuestionOptionMapper;
import com.pji.triagem.mapper.SymptomMapper;
import com.pji.triagem.model.Question;
import com.pji.triagem.model.QuestionOption;
import com.pji.triagem.model.QuestionType;
import com.pji.triagem.model.Symptom;
import com.pji.triagem.repository.SymptomRepository;
import com.pji.triagem.service.QuestionOptionService;
import com.pji.triagem.service.QuestionService;
import com.pji.triagem.service.SymptomService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class SymptomServiceImpl extends BaseServiceImpl<Symptom> implements SymptomService {

    private final SymptomRepository symptomRepository;
    private final QuestionService questionService;
    private final QuestionOptionService questionOptionService;
    private final SymptomMapper symptomMapper;
    private final QuestionMapper questionMapper;
    private final QuestionOptionMapper questionOptionMapper;

    @Override
    protected BaseRepository<Symptom, Long> getRepository() {
        return symptomRepository;
    }

    @Override
    protected String getResourceName() {
        return "Sintoma";
    }

    @Override
    @Transactional(readOnly = true)
    public List<SymptomResponse> findAllSymptoms() {
        return symptomRepository.findAllByOrderByOrderAsc().stream()
                .map(symptomMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public QuestionnaireResponse buildQuestionnaire(QuestionnaireRequest request) {
        validateQuestionnaireRequest(request);

        List<Long> distinctSymptomIds = request.getSymptomIds().stream().distinct().toList();
        List<Symptom> symptoms = symptomRepository.findAllByIdIn(distinctSymptomIds);
        validateRequestedSymptoms(distinctSymptomIds, symptoms);

        List<Question> questions = questionService.findBySymptoms(distinctSymptomIds);
        Map<Long, List<QuestionOption>> optionsByQuestionId = buildOptionsByQuestionId(questions);
        Map<Long, List<Question>> questionsBySymptomId = questions.stream()
                .collect(Collectors.groupingBy(question -> question.getSymptom().getId(), LinkedHashMap::new, Collectors.toList()));

        List<QuestionnaireSymptomResponse> symptomResponses = new ArrayList<>();
        for (Long symptomId : distinctSymptomIds) {
            Symptom symptom = symptoms.stream()
                    .filter(item -> item.getId().equals(symptomId))
                    .findFirst()
                    .orElseThrow(() -> new ValidationException("Sintoma informado não existe"));

            QuestionnaireSymptomResponse symptomResponse = symptomMapper.toQuestionnaireSymptomResponse(symptom);
            List<QuestionResponse> questionResponses = questionsBySymptomId.getOrDefault(symptomId, List.of()).stream()
                    .map(question -> toQuestionResponse(question, optionsByQuestionId.getOrDefault(question.getId(), List.of())))
                    .toList();
            symptomResponse.setQuestions(questionResponses);
            symptomResponses.add(symptomResponse);
        }

        return QuestionnaireResponse.builder()
                .symptoms(symptomResponses)
                .build();
    }

    private void validateQuestionnaireRequest(QuestionnaireRequest request) {
        if (request == null || request.getSymptomIds() == null || request.getSymptomIds().isEmpty()) {
            throw new ValidationException("A lista de sintomas é obrigatória");
        }
        if (request.getSymptomIds().stream().anyMatch(id -> id == null)) {
            throw new ValidationException("Todos os sintomas informados devem possuir id");
        }
    }

    private void validateRequestedSymptoms(List<Long> requestedSymptomIds, List<Symptom> symptoms) {
        Set<Long> existingIds = symptoms.stream()
                .map(Symptom::getId)
                .collect(Collectors.toSet());

        if (!existingIds.containsAll(requestedSymptomIds)) {
            throw new ValidationException("Um ou mais sintomas informados não existem");
        }
    }

    private Map<Long, List<QuestionOption>> buildOptionsByQuestionId(List<Question> questions) {
        List<Long> questionIds = questions.stream()
                .filter(question -> QuestionType.OPTIONS.equals(question.getType()))
                .map(Question::getId)
                .toList();

        if (questionIds.isEmpty()) {
            return Map.of();
        }

        return questionOptionService.findByQuestions(questionIds).stream()
                .collect(Collectors.groupingBy(option -> option.getQuestion().getId(), LinkedHashMap::new, Collectors.toList()));
    }

    private QuestionResponse toQuestionResponse(Question question, List<QuestionOption> options) {
        QuestionResponse response = questionMapper.toResponse(question);
        response.setOptions(options.stream().map(questionOptionMapper::toResponse).toList());
        return response;
    }
}
