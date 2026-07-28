package com.pji.triagem.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pji.triagem.mapper.AssessmentHistoryMapper;
import com.pji.triagem.mapper.AssessmentMapper;
import com.pji.triagem.repository.AssessmentRepository;
import com.pji.triagem.repository.AssessmentSymptomRepository;
import com.pji.triagem.repository.ChildRepository;
import com.pji.triagem.repository.UserRepository;
import com.pji.triagem.service.AssessmentCalculationService;
import com.pji.triagem.service.ChildService;
import com.pji.triagem.service.CurrentUserService;
import com.pji.triagem.service.QuestionOptionService;
import com.pji.triagem.service.QuestionService;
import com.pji.triagem.service.SymptomService;
import com.pji.triagem.service.YesNoWeightService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class AssessmentServiceImplAccessTest {

    @Mock
    private AssessmentRepository assessmentRepository;
    @Mock
    private AssessmentCalculationService calculationService;
    @Mock
    private ChildService childService;
    @Mock
    private SymptomService symptomService;
    @Mock
    private QuestionService questionService;
    @Mock
    private QuestionOptionService questionOptionService;
    @Mock
    private YesNoWeightService yesNoWeightService;
    @Mock
    private AssessmentMapper assessmentMapper;
    @Mock
    private AssessmentHistoryMapper assessmentHistoryMapper;
    @Mock
    private AssessmentSymptomRepository assessmentSymptomRepository;
    @Mock
    private ChildRepository childRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CurrentUserService currentUserService;

    @Test
    void getHistoryRejectsAnotherUserIdBeforeQueryingData() {
        AssessmentServiceImpl service = new AssessmentServiceImpl(
                assessmentRepository,
                calculationService,
                childService,
                symptomService,
                questionService,
                questionOptionService,
                yesNoWeightService,
                assessmentMapper,
                assessmentHistoryMapper,
                assessmentSymptomRepository,
                childRepository,
                userRepository,
                currentUserService,
                new ObjectMapper()
        );
        doThrow(new AccessDeniedException("Acesso negado"))
                .when(currentUserService)
                .assertCanAccessUser(2L);

        assertThrows(AccessDeniedException.class, () -> service.getHistory(2L, null));
        verifyNoInteractions(userRepository, childRepository, assessmentRepository, assessmentSymptomRepository);
    }
}
