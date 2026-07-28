package com.pji.triagem.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pji.triagem.base.repository.BaseRepository;
import com.pji.triagem.base.service.impl.BaseServiceImpl;
import com.pji.triagem.dto.request.CreateAssessmentAnswerRequest;
import com.pji.triagem.dto.request.CreateAssessmentRequest;
import com.pji.triagem.dto.request.CreateAssessmentSymptomRequest;
import com.pji.triagem.dto.response.AssessmentHistoryChildResponse;
import com.pji.triagem.dto.response.AssessmentHistoryItemResponse;
import com.pji.triagem.dto.response.AssessmentHistoryResponse;
import com.pji.triagem.dto.response.AssessmentResultResponse;
import com.pji.triagem.dto.response.AssessmentStatsResponse;
import com.pji.triagem.dto.response.AssessmentSymptomResultResponse;
import com.pji.triagem.exception.ResourceNotFoundException;
import com.pji.triagem.exception.ValidationException;
import com.pji.triagem.mapper.AssessmentHistoryMapper;
import com.pji.triagem.mapper.AssessmentMapper;
import com.pji.triagem.model.Assessment;
import com.pji.triagem.model.AssessmentSymptom;
import com.pji.triagem.model.Child;
import com.pji.triagem.model.Classification;
import com.pji.triagem.model.Question;
import com.pji.triagem.model.QuestionOption;
import com.pji.triagem.model.QuestionType;
import com.pji.triagem.model.Symptom;
import com.pji.triagem.model.User;
import com.pji.triagem.model.YesNoAnswer;
import com.pji.triagem.model.YesNoWeight;
import com.pji.triagem.repository.AssessmentRepository;
import com.pji.triagem.repository.AssessmentSymptomRepository;
import com.pji.triagem.repository.ChildRepository;
import com.pji.triagem.repository.UserRepository;
import com.pji.triagem.service.AssessmentCalculationService;
import com.pji.triagem.service.AssessmentService;
import com.pji.triagem.service.ChildService;
import com.pji.triagem.service.CurrentUserService;
import com.pji.triagem.service.QuestionOptionService;
import com.pji.triagem.service.QuestionService;
import com.pji.triagem.service.SymptomService;
import com.pji.triagem.service.YesNoWeightService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Comparator;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AssessmentServiceImpl extends BaseServiceImpl<Assessment> implements AssessmentService {

    private final AssessmentRepository assessmentRepository;
    private final AssessmentCalculationService calculationService;
    private final ChildService childService;
    private final SymptomService symptomService;
    private final QuestionService questionService;
    private final QuestionOptionService questionOptionService;
    private final YesNoWeightService yesNoWeightService;
    private final AssessmentMapper assessmentMapper;
    private final AssessmentHistoryMapper assessmentHistoryMapper;
    private final AssessmentSymptomRepository assessmentSymptomRepository;
    private final ChildRepository childRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final ObjectMapper objectMapper;

    @Override
    protected BaseRepository<Assessment, Long> getRepository() {
        return assessmentRepository;
    }

    @Override
    protected String getResourceName() {
        return "Avaliação";
    }

    @Override
    @Transactional
    public AssessmentResultResponse createAssessment(CreateAssessmentRequest request) {
        validateCreateRequest(request);

        Child child = childService.findAccessibleEntity(request.getChildId());
        Assessment assessment = new Assessment();
        assessment.setChild(child);

        List<Classification> classifications = new ArrayList<>();
        List<Map<String, Object>> summarySymptoms = new ArrayList<>();
        Set<Long> symptomIds = new HashSet<>();
        int totalScore = 0;
        boolean assessmentRedFlag = false;

        for (CreateAssessmentSymptomRequest symptomRequest : request.getSymptoms()) {
            if (symptomRequest.getSymptomId() == null) {
                throw new ValidationException("Sintoma é obrigatório");
            }
            if (!symptomIds.add(symptomRequest.getSymptomId())) {
                throw new ValidationException("Sintoma repetido na avaliação");
            }

            CalculatedSymptom calculated = calculateSymptom(symptomRequest);
            totalScore += calculated.score();
            assessmentRedFlag = assessmentRedFlag || calculated.redFlagDetected();
            classifications.add(calculated.classification());

            AssessmentSymptom assessmentSymptom = new AssessmentSymptom();
            assessmentSymptom.setSymptom(calculated.symptom());
            assessmentSymptom.setScore(calculated.score());
            assessmentSymptom.setClassification(calculated.classification());
            assessmentSymptom.setRedFlagDetected(calculated.redFlagDetected());
            assessmentSymptom.setResponses(toJson(calculated.responses()));
            assessment.addSymptom(assessmentSymptom);

            summarySymptoms.add(calculated.summary());
        }

        assessment.setTotalScore(totalScore);
        assessment.setRedFlagDetected(assessmentRedFlag);
        assessment.setFinalClassification(calculationService.classifyFinal(classifications));
        assessment.setResponses(toJson(summary(totalScore, assessmentRedFlag, assessment.getFinalClassification(), summarySymptoms)));

        Assessment saved = save(assessment);
        return toAssessmentResultResponse(saved, saved.getSymptoms());
    }

    @Override
    @Transactional(readOnly = true)
    public AssessmentResultResponse getAssessment(Long assessmentId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Recurso não encontrado", getResourceName()));
        currentUserService.assertCanAccessUser(assessment.getChild().getUser().getId());
        return toAssessmentResultResponse(
                assessment,
                assessmentSymptomRepository.findByAssessmentIdOrderByIdAsc(assessmentId)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AssessmentHistoryResponse getHistory(Long userId, Long childId) {
        return getHistory(userId, childId, null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public AssessmentHistoryResponse getHistory(Long userId, Long childId, Integer page, Integer size) {
        requireAccessibleUser(userId);

        List<Child> children = childRepository.findByUserId(userId).stream()
                .sorted(Comparator.comparing(Child::getName, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(Child::getId))
                .toList();

        if (childId != null) {
            childRepository.findByIdAndUserId(childId, userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Recurso não encontrado", "Criança"));
        }

        List<Assessment> allAssessments = childId == null
                ? assessmentRepository.findByChildUserIdOrderByCreatedAtDesc(userId)
                : assessmentRepository.findByChildIdAndChildUserIdOrderByCreatedAtDesc(childId, userId);
        List<Assessment> assessments = paginate(allAssessments, page, size);

        Map<Long, List<String>> symptomNamesByAssessmentId = buildSymptomNamesByAssessmentId(assessments);

        List<AssessmentHistoryChildResponse> childResponses = children.stream()
                .map(assessmentHistoryMapper::toChildResponse)
                .toList();

        List<AssessmentHistoryItemResponse> assessmentResponses = assessments.stream()
                .map(assessment -> toHistoryItemResponse(
                        assessment,
                        symptomNamesByAssessmentId.getOrDefault(assessment.getId(), List.of())
                ))
                .toList();

        return AssessmentHistoryResponse.builder()
                .totalAssessments((long) allAssessments.size())
                .lowRiskCount(countByClassification(allAssessments, Classification.LOW))
                .moderateRiskCount(countByClassification(allAssessments, Classification.MOD))
                .highRiskCount(countByClassification(allAssessments, Classification.HIGH))
                .selectedChildId(childId)
                .children(childResponses)
                .assessments(assessmentResponses)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AssessmentStatsResponse getStats(Long userId, Long childId) {
        return AssessmentStatsResponse.from(getHistory(userId, childId));
    }

    private CalculatedSymptom calculateSymptom(CreateAssessmentSymptomRequest symptomRequest) {
        if (symptomRequest.getAnswers() == null || symptomRequest.getAnswers().isEmpty()) {
            throw new ValidationException("Sintoma sem respostas");
        }

        Symptom symptom = symptomService.find(symptomRequest.getSymptomId());
        List<Question> questions = questionService.findBySymptom(symptom.getId());
        if (questions.isEmpty()) {
            throw new ValidationException("Sintoma não possui perguntas cadastradas");
        }

        Map<Long, Question> questionsById = questions.stream()
                .collect(Collectors.toMap(Question::getId, Function.identity()));
        validateRequiredQuestions(questionsById.keySet(), symptomRequest.getAnswers());

        int score = 0;
        boolean redFlagDetected = false;
        List<Map<String, Object>> answerResponses = new ArrayList<>();

        for (CreateAssessmentAnswerRequest answerRequest : symptomRequest.getAnswers()) {
            Question question = questionsById.get(answerRequest.getQuestionId());
            if (question == null) {
                throw new ValidationException("Pergunta não pertence ao sintoma informado");
            }

            CalculatedAnswer calculatedAnswer = calculateAnswer(question, answerRequest);
            score += calculatedAnswer.score();
            redFlagDetected = redFlagDetected || calculatedAnswer.redFlagDetected();
            answerResponses.add(calculatedAnswer.response());
        }

        Classification classification = calculationService.classifySymptom(score, redFlagDetected);

        Map<String, Object> responses = new LinkedHashMap<>();
        responses.put("symptomId", symptom.getId());
        responses.put("symptomCode", symptom.getCode());
        responses.put("answers", answerResponses);

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("symptomId", symptom.getId());
        summary.put("classification", classification.name());
        summary.put("score", score);
        summary.put("redFlagDetected", redFlagDetected);

        return new CalculatedSymptom(symptom, score, redFlagDetected, classification, responses, summary);
    }

    private CalculatedAnswer calculateAnswer(Question question, CreateAssessmentAnswerRequest answerRequest) {
        if (QuestionType.OPTIONS.equals(question.getType())) {
            return calculateOptionsAnswer(question, answerRequest);
        }
        if (QuestionType.YESNO.equals(question.getType())) {
            return calculateYesNoAnswer(question, answerRequest);
        }
        throw new ValidationException("Tipo de pergunta inválido");
    }

    private CalculatedAnswer calculateOptionsAnswer(Question question, CreateAssessmentAnswerRequest answerRequest) {
        if (answerRequest.getOptionId() == null) {
            throw new ValidationException("Pergunta OPTIONS exige optionId");
        }
        if (answerRequest.getAnswer() != null) {
            throw new ValidationException("Pergunta OPTIONS não deve enviar answer");
        }

        QuestionOption option = questionOptionService.findByQuestionOption(question.getId(), answerRequest.getOptionId());

        Map<String, Object> response = baseAnswerResponse(question);
        response.put("optionId", option.getId());
        response.put("optionCode", option.getCode());
        response.put("selectedValue", option.getText());
        response.put("score", option.getScore());
        response.put("redFlag", option.getRedFlag());

        return new CalculatedAnswer(option.getScore(), Boolean.TRUE.equals(option.getRedFlag()), response);
    }

    private CalculatedAnswer calculateYesNoAnswer(Question question, CreateAssessmentAnswerRequest answerRequest) {
        if (answerRequest.getAnswer() == null) {
            throw new ValidationException("Pergunta YESNO exige answer YES, NO ou DUNNO");
        }
        if (answerRequest.getOptionId() != null) {
            throw new ValidationException("Pergunta YESNO não deve enviar optionId");
        }

        YesNoAnswer answer = answerRequest.getAnswer();
        YesNoWeight weight = yesNoWeightService.findByQuestion(question.getId());
        Integer score = calculationService.scoreYesNo(answer, weight);
        Boolean redFlag = calculationService.redFlagYesNo(answer, weight);

        Map<String, Object> response = baseAnswerResponse(question);
        response.put("answer", answer.name());
        response.put("score", score);
        response.put("redFlag", redFlag);

        return new CalculatedAnswer(score, redFlag, response);
    }

    private Map<String, Object> baseAnswerResponse(Question question) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("questionId", question.getId());
        response.put("questionCode", question.getCode());
        response.put("type", question.getType().name());
        return response;
    }

    private List<Assessment> paginate(List<Assessment> assessments, Integer page, Integer size) {
        if (size == null) {
            return assessments;
        }
        if (page != null && page < 0) {
            throw new ValidationException("page deve ser maior ou igual a 0");
        }
        if (size <= 0) {
            throw new ValidationException("size deve ser maior que 0");
        }
        int safePage = page == null ? 0 : page;
        int fromIndex = safePage * size;
        if (fromIndex >= assessments.size()) {
            return List.of();
        }
        int toIndex = Math.min(fromIndex + size, assessments.size());
        return assessments.subList(fromIndex, toIndex);
    }

    private void validateCreateRequest(CreateAssessmentRequest request) {
        if (request == null) {
            throw new ValidationException("Dados da avaliação são obrigatórios");
        }
        if (request.getChildId() == null) {
            throw new ValidationException("Criança é obrigatória");
        }
        if (request.getSymptoms() == null || request.getSymptoms().isEmpty()) {
            throw new ValidationException("Avaliação sem sintomas");
        }
    }

    private void validateRequiredQuestions(Collection<Long> requiredQuestionIds, List<CreateAssessmentAnswerRequest> answers) {
        Set<Long> answeredQuestionIds = new HashSet<>();

        for (CreateAssessmentAnswerRequest answer : answers) {
            if (answer.getQuestionId() == null) {
                throw new ValidationException("Pergunta é obrigatória");
            }
            if (!answeredQuestionIds.add(answer.getQuestionId())) {
                throw new ValidationException("Pergunta repetida na avaliação do sintoma");
            }
        }

        if (!answeredQuestionIds.containsAll(requiredQuestionIds) || answeredQuestionIds.size() != requiredQuestionIds.size()) {
            throw new ValidationException("Todas as perguntas do sintoma devem ser respondidas");
        }
    }

    private Map<String, Object> summary(
            Integer totalScore,
            Boolean redFlagDetected,
            Classification finalClassification,
            List<Map<String, Object>> symptoms
    ) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalScore", totalScore);
        summary.put("redFlagDetected", redFlagDetected);
        summary.put("finalClassification", finalClassification.name());
        summary.put("symptoms", symptoms);
        return summary;
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new ValidationException("Não foi possível montar o JSON da avaliação");
        }
    }

    private AssessmentResultResponse toAssessmentResultResponse(Assessment assessment, List<AssessmentSymptom> symptoms) {
        AssessmentResultResponse response = assessmentMapper.toResultResponse(assessment);
        List<AssessmentSymptomResultResponse> symptomResponses = symptoms.stream()
                .map(assessmentMapper::toSymptomResultResponse)
                .toList();
        response.setSymptoms(symptomResponses);
        return response;
    }

    private AssessmentHistoryItemResponse toHistoryItemResponse(Assessment assessment, List<String> symptoms) {
        AssessmentHistoryItemResponse response = assessmentHistoryMapper.toItemResponse(assessment);
        response.setSymptoms(symptoms);
        return response;
    }

    private Map<Long, List<String>> buildSymptomNamesByAssessmentId(List<Assessment> assessments) {
        if (assessments.isEmpty()) {
            return Map.of();
        }

        List<Long> assessmentIds = assessments.stream()
                .map(Assessment::getId)
                .toList();

        Map<Long, List<String>> symptomNamesByAssessmentId = new LinkedHashMap<>();
        for (AssessmentSymptom assessmentSymptom : assessmentSymptomRepository.findByAssessmentIdInOrderByAssessmentIdAscIdAsc(assessmentIds)) {
            symptomNamesByAssessmentId
                    .computeIfAbsent(assessmentSymptom.getAssessment().getId(), ignored -> new ArrayList<>())
                    .add(assessmentSymptom.getSymptom().getName());
        }
        return symptomNamesByAssessmentId;
    }

    private long countByClassification(List<Assessment> assessments, Classification classification) {
        return assessments.stream()
                .filter(assessment -> classification.equals(assessment.getFinalClassification()))
                .count();
    }

    private User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Recurso não encontrado", "Usuário"));
    }

    private User requireAccessibleUser(Long userId) {
        currentUserService.assertCanAccessUser(userId);
        return requireUser(userId);
    }

    private record CalculatedAnswer(
            Integer score,
            Boolean redFlagDetected,
            Map<String, Object> response
    ) {
    }

    private record CalculatedSymptom(
            Symptom symptom,
            Integer score,
            Boolean redFlagDetected,
            Classification classification,
            Map<String, Object> responses,
            Map<String, Object> summary
    ) {
    }
}
